package com.shubham.csa.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessage {
    public enum status{
        ADDED,
        DELETED,
    }
public enum MessageType {
        COMMENT,      // Regular comment from user or agent
        INTERNAL_NOTE, // Internal note visible only to agents
        SYSTEM,       // System-generated message (status changes, assignments, etc.)
        RESOLUTION    // Resolution message when ticket is resolved
    }

    // Unique message ID within the ticket
    private String id;

    // Author information (denormalized for performance)
    private String authorId;
    private String authorName;
    private String authorEmail;
    private String authorRole; // CUSTOMER, AGENT, MANAGER, ADMIN, SYSTEM

    // Message content
    private String content;

    // Message type
    private MessageType messageType = MessageType.COMMENT;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited = false;

    // File attachments (stored as references for now)
    private List<String> attachmentIds = new ArrayList<>();

    // Reactions/likes (simple count for now)
    private int likes = 0;

 

    // Constructor with essential fields
    public TicketMessage(String authorId, String authorName, String authorEmail, 
                        String authorRole, String content, MessageType messageType) {
     
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.authorRole = authorRole;
        this.content = content;
        this.messageType = messageType;
    }

    // Helper method to create system message
    public static TicketMessage createSystemMessage(String content) {
        TicketMessage message = new TicketMessage();
        message.setAuthorId("system");
        message.setAuthorName("System");
        message.setAuthorRole("SYSTEM");
        message.setContent(content);
        message.setMessageType(MessageType.SYSTEM);
        return message;
    }

}
