package com.shubham.csa.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shubham.csa.Security.UserPrincipal;
import com.shubham.csa.Service.MessageService;
import com.shubham.csa.dto.CreateMessageDto;
import com.shubham.csa.dto.TicketDto;
import com.shubham.csa.dto.TicketMessageDto;
import com.shubham.csa.dto.UpdateMessageDto;
import com.shubham.csa.entity.TicketMessage;
import com.shubham.csa.entity.User;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
public class MessageController {
   @Autowired
    private MessageService messageService;
  
    // Add a message to a ticket
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketMessageDto> addMessage(
            @PathVariable String ticketId,
             @AuthenticationPrincipal UserPrincipal userPrincipal,
           // @RequestParam String userId, // In real app, get from security context
            @Valid @RequestBody CreateMessageDto createDto) {
        try {
            String userId = userPrincipal.getId();
            TicketMessageDto message = messageService.addMessage(ticketId, userId, createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
             e.printStackTrace(); // 👈 add this line
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all messages for a ticket
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketMessageDto>> getTicketMessages(@PathVariable String ticketId) {
        try {
            List<TicketMessageDto> messages = messageService.getTicketMessages(ticketId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get a specific message
    @GetMapping("/{messageId}")
    public ResponseEntity<TicketMessageDto> getMessage(
            @PathVariable String ticketId,
            @PathVariable String messageId) {
        try {
            TicketMessageDto message = messageService.getMessage(ticketId, messageId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update a message
    @PutMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketMessageDto> updateMessage(
            @PathVariable String ticketId,
            @PathVariable String messageId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String userId, // In real app, get from security context
            @Valid @RequestBody UpdateMessageDto updateDto) {
        try {
            String newuserId = userPrincipal.getId();
            TicketMessageDto message = messageService.updateMessage(ticketId, messageId, newuserId, updateDto);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a message
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String ticketId,
            @PathVariable String messageId,
           @AuthenticationPrincipal UserPrincipal userPrincipal) { // In real app, get from security context
        try {
            String userId = userPrincipal.getId();
            messageService.deleteMessage(ticketId, messageId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Like a message
    @PostMapping("/{messageId}/like")
    public ResponseEntity<TicketMessageDto> likeMessage(
            @PathVariable String ticketId,
            @PathVariable String messageId) {
        try {
            TicketMessageDto message = messageService.likeMessage(ticketId, messageId);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get message count
    @GetMapping("/count")
    public ResponseEntity<Integer> getMessageCount(@PathVariable String ticketId) {
        try {
            int count = messageService.getMessageCount(ticketId);
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get messages by type
    @GetMapping("/type/{messageType}")
    public ResponseEntity<List<TicketMessageDto>> getMessagesByType(
            @PathVariable String ticketId,
            @PathVariable TicketMessage.MessageType messageType) {
        try {
            List<TicketMessageDto> messages = messageService.getMessagesByType(ticketId, messageType);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
   
    @GetMapping("/type/INTERNAL_NOTE")
    @PreAuthorize("hasAnyRole('AGENT', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<TicketMessageDto>> getInternalNotes(@PathVariable String ticketId) {
        try {
            List<TicketMessageDto> messages = messageService.getMessagesByType(
                ticketId, TicketMessage.MessageType.INTERNAL_NOTE);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



}
