package com.shubham.csa.listener;

import com.shubham.csa.dto.WebhookPayload;
import com.shubham.csa.entity.Ticket;
import com.shubham.csa.entity.User;
import com.shubham.csa.entity.Webhook;
import com.shubham.csa.event.TicketEvent;
import com.shubham.csa.event.UserEvent;
import com.shubham.csa.Service.WebhookDeliveryService;
import com.shubham.csa.Service.WebhookService;
import com.shubham.csa.constants.WebhookEventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookEventListener {
    
    private final WebhookService webhookService;
    private final WebhookDeliveryService deliveryService;
    
    /**
     * Listen to all ticket events and trigger appropriate webhooks
     */
    @EventListener
    @Async
    public void handleTicketEvent(TicketEvent event) {
        log.info("Received ticket event: {} for ticket: {}", 
            event.getEventType(), event.getTicket().getTicketNumber());
        
        try {
            // Get the tenant ID from the ticket (assuming you have this)
            String tenantId = "default"; // TODO: Get from ticket or context
            
            // Determine which events to trigger
            List<String> eventTypes = determineEventTypes(event);
            
            // For each event type, trigger webhooks
            for (String eventType : eventTypes) {
                triggerWebhooksForEvent(tenantId, eventType, event);
            }
            
        } catch (Exception e) {
            log.error("Error handling ticket event", e);
        }
    }
    
    /**
     * Listen to user events
     */
    @EventListener
    @Async
    public void handleUserEvent(UserEvent event) {
        log.info("Received user event: {} for user: {}", 
            event.getEventType(), event.getUser().getEmail());
        
        try {
            String tenantId = event.getUser().getTenantId();
            triggerWebhooksForUserEvent(tenantId, event);
        } catch (Exception e) {
            log.error("Error handling user event", e);
        }
    }
    
    /**
     * Determine which webhook events to trigger based on ticket changes
     */
    private List<String> determineEventTypes(TicketEvent event) {
        List<String> eventTypes = new java.util.ArrayList<>();
        
        // Always include the primary event type
        eventTypes.add(event.getEventType());
        
        // Add additional specific events based on what changed
        if (event.isStatusChanged()) {
            // Add status-specific event
            String statusEvent = WebhookEventType.getEventTypeFromStatus(
                event.getTicket().getStatus()
            );
            if (!eventTypes.contains(statusEvent)) {
                eventTypes.add(statusEvent);
            }
            
            // Add generic status changed event
            if (!eventTypes.contains(WebhookEventType.TICKET_STATUS_CHANGED)) {
                eventTypes.add(WebhookEventType.TICKET_STATUS_CHANGED);
            }
        }
        
        if (event.isPriorityChanged()) {
            if (!eventTypes.contains(WebhookEventType.TICKET_PRIORITY_CHANGED)) {
                eventTypes.add(WebhookEventType.TICKET_PRIORITY_CHANGED);
            }
        }
        
        if (event.isAssignmentChanged()) {
            String assignmentEvent = event.getTicket().getAssignedToId() != null
                ? WebhookEventType.TICKET_ASSIGNED
                : WebhookEventType.TICKET_UNASSIGNED;
            if (!eventTypes.contains(assignmentEvent)) {
                eventTypes.add(assignmentEvent);
            }
        }
        
        return eventTypes;
    }
    
    /**
     * Trigger webhooks for a specific event type
     */
    private void triggerWebhooksForEvent(String tenantId, String eventType, TicketEvent event) {
        // Find all webhooks subscribed to this event
        List<Webhook> webhooks = webhookService.getWebhooksForEvent(tenantId, eventType);
        
        if (webhooks.isEmpty()) {
            log.debug("No webhooks found for event: {}", eventType);
            return;
        }
        
        log.info("Triggering {} webhooks for event: {}", webhooks.size(), eventType);
        
        // Build the payload data
        Map<String, Object> ticketData = buildTicketData(event.getTicket());
        Map<String, Object> previousData = event.getPreviousState() != null 
            ? buildTicketData(event.getPreviousState())
            : null;
        
        // Trigger each webhook
        for (Webhook webhook : webhooks) {
            try {
                String deliveryId = UUID.randomUUID().toString();
                
                WebhookPayload payload = WebhookPayload.builder()
                    .id(deliveryId)
                    .event(eventType)
                    .timestamp(java.time.LocalDateTime.now())
                    .webhookId(webhook.getId())
                    .data(ticketData)
                    .previousData(previousData)
                    .triggeredBy(event.getTriggeredBy())
                    .tenantId(tenantId)
                    .build();
                
                deliveryService.deliverWebhookAsync(
                    webhook, 
                    payload, 
                    event.getTicket().getId(),
                    event.getTicket().getTicketNumber()
                );
                
            } catch (Exception e) {
                log.error("Error triggering webhook: {}", webhook.getId(), e);
            }
        }
    }
    
    /**
     * Trigger webhooks for user events
     */
    private void triggerWebhooksForUserEvent(String tenantId, UserEvent event) {
        List<Webhook> webhooks = webhookService.getWebhooksForEvent(tenantId, event.getEventType());
        
        if (webhooks.isEmpty()) {
            log.debug("No webhooks found for user event: {}", event.getEventType());
            return;
        }
        
        log.info("Triggering {} webhooks for user event: {}", webhooks.size(), event.getEventType());
        
        Map<String, Object> userData = buildUserData(event.getUser());
        
        for (Webhook webhook : webhooks) {
            try {
                String deliveryId = UUID.randomUUID().toString();
                
                WebhookPayload payload = WebhookPayload.builder()
                    .id(deliveryId)
                    .event(event.getEventType())
                    .timestamp(java.time.LocalDateTime.now())
                    .webhookId(webhook.getId())
                    .data(userData)
                    .triggeredBy(event.getTriggeredBy())
                    .tenantId(tenantId)
                    .build();
                
                // User events don't have ticket context
                deliveryService.deliverWebhookAsync(webhook, payload, null, null);
                
            } catch (Exception e) {
                log.error("Error triggering webhook for user event: {}", webhook.getId(), e);
            }
        }
    }
    
    /**
     * Build ticket data map for webhook payload
     */
    private Map<String, Object> buildTicketData(Ticket ticket) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", ticket.getId());
        data.put("ticketNumber", ticket.getTicketNumber());
        data.put("title", ticket.getTitle());
        data.put("description", ticket.getDescription());
        data.put("status", ticket.getStatus().toString());
        data.put("priority", ticket.getPriority().toString());
        data.put("type", ticket.getType().toString());
        data.put("customerId", ticket.getCustomerId());
        data.put("customerName", ticket.getCustomerName());
        data.put("customerEmail", ticket.getCustomerEmail());
        data.put("assignedToId", ticket.getAssignedToId());
        data.put("assignedToName", ticket.getAssignedToName());
        data.put("tags", ticket.getTags());
        data.put("messageCount", ticket.getMessageCount());
        data.put("createdAt", ticket.getCreatedAt());
        data.put("updatedAt", ticket.getUpdatedAt());
        data.put("lastMessageAt", ticket.getLastMessageAt());
        data.put("lastMessageBy", ticket.getLastMessageBy());
        return data;
    }
    
    /**
     * Build user data map for webhook payload
     */
    private Map<String, Object> buildUserData(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole().toString());
        data.put("active", user.isActive());
        data.put("tenantId", user.getTenantId());
        data.put("createdAt", user.getCreatedAt());
        data.put("updatedAt", user.getUpdatedAt());
        return data;
    }
}