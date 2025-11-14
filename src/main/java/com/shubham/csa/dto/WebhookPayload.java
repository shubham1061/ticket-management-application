package com.shubham.csa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookPayload {
  private String id;              // Unique delivery ID
    private String event;           // Event type (e.g., "ticket.created")
    private LocalDateTime timestamp; // When the event occurred
    private String webhookId;       // ID of the webhook being triggered
    
    // Event data
    private Object data;            // The actual data (Ticket, User, etc.)
    
    // Previous state (for update events)
    private Object previousData;    // Previous state before update
    
    // Additional context
    private String triggeredBy;     // User who triggered the event
    private String tenantId;        // Tenant ID for multi-tenancy
    
    // Helper method to create payload for ticket events
    public static WebhookPayload forTicket(String deliveryId, String eventType, 
                                           Object ticketData, Object previousData, 
                                           String webhookId, String triggeredBy) {
        return WebhookPayload.builder()
                .id(deliveryId)
                .event(eventType)
                .timestamp(LocalDateTime.now())
                .webhookId(webhookId)
                .data(ticketData)
                .previousData(previousData)
                .triggeredBy(triggeredBy)
                .build();
    }
    
    // Helper method to create payload for user events
    public static WebhookPayload forUser(String deliveryId, String eventType, 
                                         Object userData, String webhookId, 
                                         String triggeredBy) {
        return WebhookPayload.builder()
                .id(deliveryId)
                .event(eventType)
                .timestamp(LocalDateTime.now())
                .webhookId(webhookId)
                .data(userData)
                .triggeredBy(triggeredBy)
                .build();
    }
}
