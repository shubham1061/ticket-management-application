package com.shubham.csa.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shubham.csa.dto.WebhookDeliveryResponse;
import com.shubham.csa.dto.WebhookPayload;
import com.shubham.csa.entity.Webhook;
import com.shubham.csa.entity.WebhookDelivery;
import com.shubham.csa.entity.WebhookDelivery.DeliveryStatus;
import com.shubham.csa.Repository.WebhookDeliveryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;



@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {
    
    private final WebhookDeliveryRepository deliveryRepository;
    private final WebhookService webhookService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    private static final int TIMEOUT_SECONDS = 30;
    
    /**
     * Deliver webhook asynchronously
     */
    @Async
    public void deliverWebhookAsync(Webhook webhook, WebhookPayload payload, String ticketId, String ticketNumber) {
        deliverWebhook(webhook, payload, ticketId, ticketNumber);
    }
    
    /**
     * Deliver webhook synchronously
     */
    public void deliverWebhook(Webhook webhook, WebhookPayload payload, String ticketId, String ticketNumber) {
        // Create delivery record
        WebhookDelivery delivery = createDeliveryRecord(webhook, payload, ticketId, ticketNumber);
        
        try {
            // Convert payload to JSON
            String payloadJson = objectMapper.writeValueAsString(payload);
            delivery.setPayload(payloadJson);
            
            // Generate signature
            String signature = generateSignature(payloadJson, webhook.getSecret());
            
            // Build HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set(SIGNATURE_HEADER, "sha256=" + signature);
            headers.set("X-Webhook-Id", webhook.getId());
            headers.set("X-Event-Type", payload.getEvent());
            
            // Add custom headers
            if (webhook.getCustomHeaders() != null) {
                webhook.getCustomHeaders().forEach(headers::set);
            }
            
            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);
            
            // Send request
            long startTime = System.currentTimeMillis();
            delivery.incrementAttempt();
            
            ResponseEntity<String> response = restTemplate.exchange(
                webhook.getUrl(),
                HttpMethod.POST,
                entity,
                String.class
            );
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Handle success
            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.markAsSuccess(
                    response.getStatusCode().value(),
                    response.getBody(),
                    responseTime
                );
                webhookService.updateWebhookStats(webhook.getId(), true);
                log.info("Webhook delivered successfully: {} to {}", delivery.getId(), webhook.getUrl());
            } else {
                handleDeliveryFailure(delivery, webhook, "Unexpected status code: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            handleDeliveryFailure(delivery, webhook, e.getMessage());
            log.error("Webhook delivery failed: {} to {}", delivery.getId(), webhook.getUrl(), e);
        } finally {
            deliveryRepository.save(delivery);
        }
    }
    
    /**
     * Handle delivery failure and schedule retry
     */
    private void handleDeliveryFailure(WebhookDelivery delivery, Webhook webhook, String errorMessage) {
        delivery.setErrorMessage(errorMessage);
        
        if (delivery.canRetry()) {
            // Calculate exponential backoff: 60s, 120s, 240s, etc.
            int delaySeconds = webhook.getRetryDelaySeconds() * (int) Math.pow(2, delivery.getAttemptCount() - 1);
            delivery.scheduleRetry(delaySeconds);
            log.warn("Scheduling retry #{} for delivery {} in {} seconds", 
                delivery.getAttemptCount(), delivery.getId(), delaySeconds);
        } else {
            delivery.markAsFailed("Max retries exceeded");
            webhookService.updateWebhookStats(webhook.getId(), false);
            log.error("Webhook delivery failed permanently: {} after {} attempts", 
                delivery.getId(), delivery.getAttemptCount());
        }
    }
    
    /**
     * Generate HMAC SHA256 signature
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac hmac = Mac.getInstance(SIGNATURE_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), 
                SIGNATURE_ALGORITHM
            );
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to generate signature", e);
            throw new RuntimeException("Signature generation failed", e);
        }
    }
    
    /**
     * Create initial delivery record
     */
    private WebhookDelivery createDeliveryRecord(Webhook webhook, WebhookPayload payload, 
                                                  String ticketId, String ticketNumber) {
        return WebhookDelivery.builder()
                .webhookId(webhook.getId())
                .webhookName(webhook.getName())
                .webhookUrl(webhook.getUrl())
                .eventType(payload.getEvent())
                .ticketId(ticketId)
                .ticketNumber(ticketNumber)
                .status(DeliveryStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(webhook.getMaxRetries())
                .build();
    }
    
    /**
     * Get deliveries by webhook
     */
    public Page<WebhookDeliveryResponse> getDeliveriesByWebhook(String webhookId, Pageable pageable) {
        return deliveryRepository.findByWebhookId(webhookId, pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Get delivery by ID
     */
    public WebhookDeliveryResponse getDeliveryById(String id) {
        WebhookDelivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));
        return mapToResponse(delivery);
    }
    
    /**
     * Manually retry a delivery
     */
    public void retryDelivery(String deliveryId) {
        WebhookDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + deliveryId));
        
        if (!delivery.canRetry()) {
            throw new IllegalStateException("Delivery cannot be retried");
        }
        
        Webhook webhook = webhookService.getWebhookEntity(delivery.getWebhookId());
        
        // Parse payload back to object
        try {
            WebhookPayload payload = objectMapper.readValue(delivery.getPayload(), WebhookPayload.class);
            deliverWebhookAsync(webhook, payload, delivery.getTicketId(), delivery.getTicketNumber());
        } catch (Exception e) {
            log.error("Failed to retry delivery", e);
            throw new RuntimeException("Retry failed", e);
        }
    }
    
    /**
     * Process pending retries (called by scheduled task)
     */
    public void processPendingRetries() {
        List<WebhookDelivery> pendingRetries = deliveryRepository.findPendingRetries(
            DeliveryStatus.RETRYING, 
            LocalDateTime.now()
        );
        
        log.info("Processing {} pending retries", pendingRetries.size());
        
        pendingRetries.forEach(delivery -> {
            try {
                Webhook webhook = webhookService.getWebhookEntity(delivery.getWebhookId());
                WebhookPayload payload = objectMapper.readValue(delivery.getPayload(), WebhookPayload.class);
                deliverWebhook(webhook, payload, delivery.getTicketId(), delivery.getTicketNumber());
            } catch (Exception e) {
                log.error("Failed to process retry for delivery: {}", delivery.getId(), e);
            }
        });
    }
    
    // Helper method to map to response
    private WebhookDeliveryResponse mapToResponse(WebhookDelivery delivery) {
        return WebhookDeliveryResponse.builder()
                .id(delivery.getId())
                .webhookId(delivery.getWebhookId())
                .webhookName(delivery.getWebhookName())
                .webhookUrl(delivery.getWebhookUrl())
                .eventType(delivery.getEventType())
                .ticketId(delivery.getTicketId())
                .ticketNumber(delivery.getTicketNumber())
                .status(delivery.getStatus())
                .attemptCount(delivery.getAttemptCount())
                .maxAttempts(delivery.getMaxAttempts())
                .responseCode(delivery.getResponseCode())
                .responseBody(delivery.getResponseBody())
                .errorMessage(delivery.getErrorMessage())
                .responseTimeMs(delivery.getResponseTimeMs())
                .createdAt(delivery.getCreatedAt())
                .nextRetryAt(delivery.getNextRetryAt())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }
}
