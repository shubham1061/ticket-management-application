package com.shubham.csa.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Document(collection = "tickets")

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@CompoundIndexes({
    @CompoundIndex(name = "tenant_status_idx", def = "{'tenantId': 1, 'status': 1}"),
    @CompoundIndex(name = "tenant_priority_idx", def = "{'tenantId': 1, 'priority': 1}"),
    @CompoundIndex(name = "tenant_assigned_idx", def = "{'tenantId': 1, 'assignedToId': 1}"),
    @CompoundIndex(name = "tenant_customer_idx", def = "{'tenantId': 1, 'customerId': 1}")
})
public class Ticket {


public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum Type {
        BUG, FEATURE_REQUEST, SUPPORT, OTHER
    }

    @Id
    private String id;

    @Indexed
    private String tenantId = "default";
    // Auto-generated ticket number (e.g., TCK-001, TCK-002)
    @Indexed(unique = true)
    private String ticketNumber;

    @TextIndexed(weight = 3) // Higher weight for better search
    private String title;

    @TextIndexed(weight = 2)
    private String description;

    @Indexed
    private Status status = Status.OPEN;

    @Indexed
    private Priority priority = Priority.MEDIUM;

    private Type type = Type.SUPPORT;

    // Customer who created the ticket
    @Indexed
    private String customerId;
    private String customerName; // Denormalized for faster display
    private String customerEmail; // Denormalized for faster display

    // Agent assigned to the ticket (optional)
    @Indexed
    private String assignedToId;
    private String assignedToName; // Denormalized for faster display

    // Tags for categorization
    private Set<String> tags = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
    
      // NEW: Embedded messages for conversation
    private List<TicketMessage> messages = new ArrayList<>();

    // NEW: Message count for quick reference
    private int messageCount = 0;

    // NEW: Last message info for quick display
    private LocalDateTime lastMessageAt;
    private String lastMessageBy;

    
    public Ticket(String title, String description, String customerId, String customerName, String customerEmail) {
        this.title = title;
        this.description = description;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.ticketNumber = generateTicketNumber();
    }
      private String generateTicketNumber() {
        return "TCK-" + System.currentTimeMillis() % 100000;
    }

     public void addMessage(TicketMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        this.messageCount = this.messages.size();
        this.lastMessageAt = message.getCreatedAt();
        this.lastMessageBy = message.getAuthorName();
    }
  

  
}
