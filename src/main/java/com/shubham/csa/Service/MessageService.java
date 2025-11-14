package com.shubham.csa.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.shubham.csa.Repository.TicketRepository;
import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.constants.WebhookEventType;
import com.shubham.csa.dto.CreateMessageDto;
import com.shubham.csa.dto.TicketMessageDto;
import com.shubham.csa.dto.UpdateMessageDto;
import com.shubham.csa.entity.Ticket;
import com.shubham.csa.entity.TicketMessage;
import com.shubham.csa.entity.User;
import com.shubham.csa.event.TicketEvent;
import com.shubham.csa.event.UserEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageService {
  @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Add a message to a ticket
    public TicketMessageDto addMessage(String ticketId, String userId, CreateMessageDto createDto) {
        // Find the ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Find the user (author)
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Create the message
        TicketMessage message = new TicketMessage(
            author.getId(),
            author.getName(),
            author.getEmail(),
            author.getRole().toString(),
            createDto.getContent(),
            createDto.getMessageType()
        );

        // Add attachment IDs if provided
        if (createDto.getAttachmentIds() != null && !createDto.getAttachmentIds().isEmpty()) {
            message.setAttachmentIds(createDto.getAttachmentIds());
        }

        // Add message to ticket
        ticket.addMessage(message);

        // If customer is replying to a closed/resolved ticket, reopen it
        if (author.getRole() == User.Role.CUSTOMER && 
            (ticket.getStatus() == Ticket.Status.CLOSED || ticket.getStatus() == Ticket.Status.RESOLVED)) {
            ticket.setStatus(Ticket.Status.OPEN);
            
            // Add system message about reopening
            TicketMessage systemMessage = TicketMessage.createSystemMessage(
                "Ticket reopened due to new customer message"
            );
            ticket.addMessage(systemMessage);
        }

        // Save the ticket with the new message
        ticketRepository.save(ticket);

       String eventType;
       String addedBy= author.getName();
        switch (message.getMessageType()) {
            case COMMENT:
                eventType = WebhookEventType.TICKET_COMMENT_ADDED;
                log.info("Publishing ticket.comment.added event for ticket: {}", ticket.getTicketNumber());
                break;
            case INTERNAL_NOTE:
                eventType = WebhookEventType.TICKET_INTERNAL_NOTE_ADDED;
                log.info("Publishing ticket.internal_note.added event for ticket: {}", ticket.getTicketNumber());
                break;
            default:
                eventType = WebhookEventType.TICKET_MESSAGE_ADDED;
                log.info("Publishing ticket.message.added event for ticket: {}", ticket.getTicketNumber());
        }
        
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, eventType, addedBy)
        );
        return convertMessageToDto(message);
    }

    // Get all messages for a ticket
    public List<TicketMessageDto> getTicketMessages(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        if (ticket.getMessages() == null || ticket.getMessages().isEmpty()) {
            return List.of();
        }

        return ticket.getMessages().stream()
                .map(this::convertMessageToDto)
                .collect(Collectors.toList());
    }

    // Get a specific message from a ticket
    public TicketMessageDto getMessage(String ticketId, String messageId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        TicketMessage message = ticket.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        return convertMessageToDto(message);
    }

    // Update a message
    public TicketMessageDto updateMessage(String ticketId, String messageId, String userId, UpdateMessageDto updateDto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Find the message
        TicketMessage message = ticket.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        // Verify the user is the author or an admin
        if (!message.getAuthorId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MANAGER) {
                throw new RuntimeException("You can only edit your own messages");
            }
        }

        // Don't allow editing system messages
        if (message.getMessageType() == TicketMessage.MessageType.SYSTEM) {
            throw new RuntimeException("System messages cannot be edited");
        }

        // Update the message
        message.setContent(updateDto.getContent());
        message.setUpdatedAt(LocalDateTime.now());
        message.setEdited(true);
        String updatedBy= message.getAuthorName();
        // Save the ticket
        ticketRepository.save(ticket);
          log.info("Publishing ticket.deleted event for ticket: {}", ticket.getTicketNumber());
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, WebhookEventType.TICKET_MESSAGE_UPDATED, updatedBy)
        );

        return convertMessageToDto(message);
    }

    // Delete a message (soft delete - mark as deleted)
    public void deleteMessage(String ticketId, String messageId, String userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        // Find the message
        TicketMessage message = ticket.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        // Verify the user is the author or an admin
        if (!message.getAuthorId().equals(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.MANAGER) {
                throw new RuntimeException("You can only delete your own messages");
            }
        }

        // Don't allow deleting system messages
        if (message.getMessageType() == TicketMessage.MessageType.SYSTEM) {
            throw new RuntimeException("System messages cannot be deleted");
        }

        // Remove the message from the ticket
        ticket.getMessages().remove(message);
        ticket.setMessageCount(ticket.getMessages().size());

        // Update last message info
        if (!ticket.getMessages().isEmpty()) {
            TicketMessage lastMessage = ticket.getMessages().get(ticket.getMessages().size() - 1);
            ticket.setLastMessageAt(lastMessage.getCreatedAt());
            ticket.setLastMessageBy(lastMessage.getAuthorName());
        } else {
            ticket.setLastMessageAt(null);
            ticket.setLastMessageBy(null);
        }
         String deletedBy= message.getAuthorName();
        // Save the ticket
        ticketRepository.save(ticket);
      
         log.info("Publishing ticket.deleted event for ticket: {}", ticket.getTicketNumber());
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, WebhookEventType.TICKET_DELETED, deletedBy)
        );
    }

    // Like a message
    public TicketMessageDto likeMessage(String ticketId, String messageId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        TicketMessage message = ticket.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));

        message.setLikes(message.getLikes() + 1);
        ticketRepository.save(ticket);

        return convertMessageToDto(message);
    }

    // Get message count for a ticket
    public int getMessageCount(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        return ticket.getMessageCount();
    }

    // Get messages by type (e.g., only internal notes)
    public List<TicketMessageDto> getMessagesByType(String ticketId, TicketMessage.MessageType messageType) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        return ticket.getMessages().stream()
                .filter(m -> m.getMessageType() == messageType)
                .map(this::convertMessageToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert TicketMessage entity to DTO
    private TicketMessageDto convertMessageToDto(TicketMessage message) {
        TicketMessageDto dto = new TicketMessageDto();
        dto.setId(message.getId());
        dto.setAuthorId(message.getAuthorId());
        dto.setAuthorName(message.getAuthorName());
        dto.setAuthorEmail(message.getAuthorEmail());
        dto.setAuthorRole(message.getAuthorRole());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        dto.setEdited(message.isEdited());
        dto.setAttachmentIds(message.getAttachmentIds());
        dto.setLikes(message.getLikes());
        return dto;
    }
}


