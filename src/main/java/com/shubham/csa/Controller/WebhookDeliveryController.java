package com.shubham.csa.Controller;

import com.shubham.csa.dto.WebhookDeliveryResponse;
import com.shubham.csa.Service.WebhookDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook-deliveries")
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryController {
    
    private final WebhookDeliveryService deliveryService;
    
    /**
     * Get deliveries for a webhook
     * GET /api/webhook-deliveries?webhookId={id}
     */
    @GetMapping
    public ResponseEntity<Page<WebhookDeliveryResponse>> getDeliveries(
            @RequestParam String webhookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching deliveries for webhook: {}", webhookId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<WebhookDeliveryResponse> deliveries = deliveryService.getDeliveriesByWebhook(webhookId, pageRequest);
        return ResponseEntity.ok(deliveries);
    }
    
    /**
     * Get delivery by ID
     * GET /api/webhook-deliveries/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebhookDeliveryResponse> getDelivery(@PathVariable String id) {
        log.info("Fetching delivery: {}", id);
        WebhookDeliveryResponse delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(delivery);
    }
    
    /**
     * Retry a failed delivery
     * POST /api/webhook-deliveries/{id}/retry
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<String> retryDelivery(@PathVariable String id) {
        log.info("Manually retrying delivery: {}", id);
        deliveryService.retryDelivery(id);
        return ResponseEntity.ok("Delivery retry queued");
    }
}
