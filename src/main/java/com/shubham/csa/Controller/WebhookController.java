package com.shubham.csa.Controller;

import com.shubham.csa.dto.WebhookRequest;
import com.shubham.csa.dto.WebhookResponse;
import com.shubham.csa.Service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    
    private final WebhookService webhookService;
    
    // TODO: Replace with actual authentication
    private static final String DEFAULT_TENANT = "default";
    
    /**
     * Create a new webhook
     * POST /api/webhooks
     */
    @PostMapping
    public ResponseEntity<WebhookResponse> createWebhook(
            @Valid @RequestBody WebhookRequest request) {
        log.info("Creating webhook: {}", request.getName());
        WebhookResponse response = webhookService.createWebhook(request, DEFAULT_TENANT);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get all webhooks
     * GET /api/webhooks
     */
    @GetMapping
    public ResponseEntity<List<WebhookResponse>> getAllWebhooks(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("Fetching all webhooks (activeOnly: {})", activeOnly);
        List<WebhookResponse> webhooks = activeOnly 
            ? webhookService.getActiveWebhooks(DEFAULT_TENANT)
            : webhookService.getAllWebhooks(DEFAULT_TENANT);
        return ResponseEntity.ok(webhooks);
    }
    
    /**
     * Get webhook by ID
     * GET /api/webhooks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebhookResponse> getWebhook(@PathVariable String id) {
        log.info("Fetching webhook: {}", id);
        WebhookResponse response = webhookService.getWebhook(id, DEFAULT_TENANT);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update webhook
     * PUT /api/webhooks/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WebhookResponse> updateWebhook(
            @PathVariable String id,
            @Valid @RequestBody WebhookRequest request) {
        log.info("Updating webhook: {}", id);
        WebhookResponse response = webhookService.updateWebhook(id, request, DEFAULT_TENANT);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete webhook
     * DELETE /api/webhooks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebhook(@PathVariable String id) {
        log.info("Deleting webhook: {}", id);
        webhookService.deleteWebhook(id, DEFAULT_TENANT);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Toggle webhook active status
     * PATCH /api/webhooks/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<WebhookResponse> toggleWebhook(@PathVariable String id) {
        log.info("Toggling webhook status: {}", id);
        WebhookResponse response = webhookService.toggleWebhookStatus(id, DEFAULT_TENANT);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Regenerate webhook secret
     * POST /api/webhooks/{id}/regenerate-secret
     */
    @PostMapping("/{id}/regenerate-secret")
    public ResponseEntity<WebhookResponse> regenerateSecret(@PathVariable String id) {
        log.info("Regenerating secret for webhook: {}", id);
        WebhookResponse response = webhookService.regenerateSecret(id, DEFAULT_TENANT);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test webhook by sending a test payload
     * POST /api/webhooks/{id}/test
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<String> testWebhook(@PathVariable String id) {
        log.info("Testing webhook: {}", id);
        // Will be implemented in WebhookDeliveryService
        return ResponseEntity.ok("Test webhook request queued");
    }
}
