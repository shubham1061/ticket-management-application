package com.shubham.csa.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "webhook_deliveries")

@CompoundIndexes({
    @CompoundIndex(name = "webhook_status_idx", def = "{'webhookId': 1, 'status': 1}"),
    @CompoundIndex(name = "webhook_event_idx", def = "{'webhookId': 1, 'eventType': 1}"),
    @CompoundIndex(name = "status_created_idx", def = "{'status': 1, 'createdAt': -1}")
})
public class WebhookDelivery {
    
    public enum DeliveryStatus {
        PENDING,    // Waiting to be sent
        SUCCESS,    // Successfully delivered (2xx response)
        FAILED,     // Failed after all retry attempts
        RETRYING    // Currently in retry process
    }
    
    @Id
    private String id;
    
    @Indexed
    private String webhookId;
    
    private String webhookName; // Denormalized for quick display
    private String webhookUrl;  // Denormalized for quick display
    
    @Indexed
    private String eventType;
    
    // The ticket ID that triggered this webhook (for tracking)
    @Indexed
    private String ticketId;
    private String ticketNumber; // Denormalized for quick display
    
    // JSON payload sent to webhook
    private String payload;
    
    @Indexed
    private DeliveryStatus status = DeliveryStatus.PENDING;
    
    // Retry information
    private int attemptCount = 0;
    private int maxAttempts = 3;
    
    // HTTP response information
    private Integer responseCode;
    private String responseBody;
    private String errorMessage;
    
    // Timing information
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime nextRetryAt;
    private LocalDateTime deliveredAt;
    
    // Request/response timing (in milliseconds)
    private Long responseTimeMs;
    
    // Helper methods
    public void incrementAttempt() {
        this.attemptCount++;
    }
    
    public boolean canRetry() {
        return attemptCount < maxAttempts && 
               (status == DeliveryStatus.PENDING || status == DeliveryStatus.FAILED);
    }
    
    public void markAsSuccess(int responseCode, String responseBody, long responseTimeMs) {
        this.status = DeliveryStatus.SUCCESS;
        this.responseCode = responseCode;
        this.responseBody = responseBody;
        this.responseTimeMs = responseTimeMs;
        this.deliveredAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = DeliveryStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    public void scheduleRetry(int delaySeconds) {
        this.status = DeliveryStatus.RETRYING;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
    }
}