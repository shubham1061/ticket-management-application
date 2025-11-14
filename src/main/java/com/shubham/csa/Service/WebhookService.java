package com.shubham.csa.Service;

import com.shubham.csa.dto.WebhookRequest;
import com.shubham.csa.dto.WebhookResponse;
import com.shubham.csa.entity.Webhook;
import com.shubham.csa.Repository.WebhookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final WebhookRepository webhookRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Create a new webhook
     */
    @Transactional
    public WebhookResponse createWebhook(WebhookRequest request, String tenantId) {
        log.info("Creating webhook: {} for tenant: {}", request.getName(), tenantId);
        
        // Check if webhook with same name already exists
        webhookRepository.findByTenantIdAndNameIgnoreCase(tenantId, request.getName())
            .ifPresent(w -> {
                throw new IllegalArgumentException("Webhook with name '" + request.getName() + "' already exists");
            });
        
        Webhook webhook = Webhook.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .secret(generateSecret())
                .eventTypes(request.getEventTypes())
                .customHeaders(request.getCustomHeaders())
                .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
                .retryDelaySeconds(request.getRetryDelaySeconds() != null ? request.getRetryDelaySeconds() : 60)
                .active(request.isActive())
                .build();
        
        Webhook saved = webhookRepository.save(webhook);
        log.info("Webhook created successfully with ID: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Update an existing webhook
     */
    @Transactional
    public WebhookResponse updateWebhook(String webhookId, WebhookRequest request, String tenantId) {
        log.info("Updating webhook: {}", webhookId);
        
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));
        
        // Verify tenant ownership
        if (!webhook.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Webhook does not belong to tenant");
        }
        
        webhook.setName(request.getName());
        webhook.setDescription(request.getDescription());
        webhook.setUrl(request.getUrl());
        webhook.setEventTypes(request.getEventTypes());
        webhook.setCustomHeaders(request.getCustomHeaders());
        webhook.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : webhook.getMaxRetries());
        webhook.setRetryDelaySeconds(request.getRetryDelaySeconds() != null ? request.getRetryDelaySeconds() : webhook.getRetryDelaySeconds());
        webhook.setActive(request.isActive());
        
        Webhook updated = webhookRepository.save(webhook);
        log.info("Webhook updated successfully: {}", webhookId);
        
        return mapToResponse(updated);
    }
    
    /**
     * Get webhook by ID
     */
    public WebhookResponse getWebhook(String webhookId, String tenantId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));
        
        if (!webhook.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Webhook does not belong to tenant");
        }
        
        return mapToResponse(webhook);
    }
    
    /**
     * Get all webhooks for a tenant
     */
    public List<WebhookResponse> getAllWebhooks(String tenantId) {
        return webhookRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active webhooks for a tenant
     */
    public List<WebhookResponse> getActiveWebhooks(String tenantId) {
        return webhookRepository.findByTenantIdAndActiveTrue(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get webhooks subscribed to a specific event
     */
    public List<Webhook> getWebhooksForEvent(String tenantId, String eventType) {
        return webhookRepository.findByTenantIdAndActiveTrueAndEventTypesContaining(tenantId, eventType);
    }
    
    /**
     * Delete a webhook
     */
    @Transactional
    public void deleteWebhook(String webhookId, String tenantId) {
        log.info("Deleting webhook: {}", webhookId);
        
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));
        
        if (!webhook.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Webhook does not belong to tenant");
        }
        
        webhookRepository.delete(webhook);
        log.info("Webhook deleted successfully: {}", webhookId);
    }
    
    /**
     * Toggle webhook active status
     */
    @Transactional
    public WebhookResponse toggleWebhookStatus(String webhookId, String tenantId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));
        
        if (!webhook.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Webhook does not belong to tenant");
        }
        
        webhook.setActive(!webhook.isActive());
        Webhook updated = webhookRepository.save(webhook);
        
        log.info("Webhook {} status toggled to: {}", webhookId, updated.isActive());
        return mapToResponse(updated);
    }
    
    /**
     * Regenerate webhook secret
     */
    @Transactional
    public WebhookResponse regenerateSecret(String webhookId, String tenantId) {
        Webhook webhook = webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));
        
        if (!webhook.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Webhook does not belong to tenant");
        }
        
        webhook.setSecret(generateSecret());
        Webhook updated = webhookRepository.save(webhook);
        
        log.info("Webhook secret regenerated for: {}", webhookId);
        return mapToResponse(updated);
    }
    
    /**
     * Update webhook statistics
     */
    @Transactional
    public void updateWebhookStats(String webhookId, boolean success) {
        webhookRepository.findById(webhookId).ifPresent(webhook -> {
            webhook.incrementDeliveryStats(success);
            webhookRepository.save(webhook);
        });
    }
    
    // Helper methods
    
    private String generateSecret() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return "whsec_" + Base64.getEncoder().encodeToString(randomBytes);
    }
    public Webhook getWebhookEntity(String webhookId) {
        return webhookRepository.findById(webhookId)
                .orElseThrow(() -> new IllegalArgumentException("Webhook not found: " + webhookId));
    }
    private WebhookResponse mapToResponse(Webhook webhook) {
        return WebhookResponse.builder()
                .id(webhook.getId())
                .tenantId(webhook.getTenantId())
                .name(webhook.getName())
                .description(webhook.getDescription())
                .url(webhook.getUrl())
                .secret(webhook.getSecret())
                .eventTypes(webhook.getEventTypes())
                .customHeaders(webhook.getCustomHeaders())
                .maxRetries(webhook.getMaxRetries())
                .retryDelaySeconds(webhook.getRetryDelaySeconds())
                .active(webhook.isActive())
                .totalDeliveries(webhook.getTotalDeliveries())
                .successfulDeliveries(webhook.getSuccessfulDeliveries())
                .failedDeliveries(webhook.getFailedDeliveries())
                .successRate(webhook.getSuccessRate())
                .lastDeliveryAt(webhook.getLastDeliveryAt())
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .build();
    }
}