package com.shubham.csa.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.shubham.csa.Repository.TicketRepository;
import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.constants.WebhookEventType;
import com.shubham.csa.dto.CreateTicketDto;
import com.shubham.csa.dto.TicketDto;
import com.shubham.csa.dto.TicketMessageDto;
import com.shubham.csa.dto.TicketStatsDto;
import com.shubham.csa.dto.UpdateTicketDto;
import com.shubham.csa.entity.Ticket;
import com.shubham.csa.entity.TicketMessage;
import com.shubham.csa.entity.User;
import com.shubham.csa.event.TicketEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TicketService {
@Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;
     
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    // Create a new ticket
    @Transactional
    public TicketDto createTicket(CreateTicketDto createDto) {
        // Find or validate customer
        User customer = null;
        
        if (createDto.getCustomerId() != null) {
            // Use provided customer ID
            customer = userRepository.findById(createDto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + createDto.getCustomerId()));
        } else if (createDto.getCustomerEmail() != null) {
            // Find customer by email or create a new one
            Optional<User> existingCustomer = userRepository.findByEmail(createDto.getCustomerEmail());
            if (existingCustomer.isPresent()) {
                customer = existingCustomer.get();
            } else {
                // Create new customer
                String customerName = createDto.getCustomerName() != null ? 
                    createDto.getCustomerName() : createDto.getCustomerEmail();
                customer = new User(customerName, createDto.getCustomerEmail(), User.Role.CUSTOMER);
                customer = userRepository.save(customer);
            }
        } else {
            throw new RuntimeException("Either customerId or customerEmail must be provided");
        }

        // Create ticket
        Ticket ticket = new Ticket(
            createDto.getTitle(), 
            createDto.getDescription(), 
            customer.getId(),
            customer.getName(),
            customer.getEmail()
        );
        
        ticket.setPriority(createDto.getPriority());
        ticket.setType(createDto.getType());
        
        if (createDto.getTags() != null) {
            ticket.setTags(createDto.getTags());
        }

        ticket = ticketRepository.save(ticket);
        // webhook integration for ticket creation
        log.info("Publishing ticket.created event for ticket: {}", ticket.getTicketNumber());
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, WebhookEventType.TICKET_CREATED, customer.getEmail())
        );
        return convertToDto(ticket);
    }

    // Get ticket by ID
    public TicketDto getTicketById(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        return convertToDto(ticket);
    }

    // Get ticket by ticket number
    public TicketDto getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found with number: " + ticketNumber));
        return convertToDto(ticket);
    }

    // Get all tickets with pagination
    public Page<TicketDto> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::convertToDto);
    }

    // Get tickets by customer
    public Page<TicketDto> getTicketsByCustomer(String customerId, Pageable pageable) {
        return ticketRepository.findByCustomerId(customerId, pageable).map(this::convertToDto);
    }

    // Get tickets assigned to an agent
    public Page<TicketDto> getTicketsByAgent(String agentId, Pageable pageable) {
        return ticketRepository.findByAssignedToId(agentId, pageable).map(this::convertToDto);
    }

    // Get tickets by status
    public Page<TicketDto> getTicketsByStatus(Ticket.Status status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable).map(this::convertToDto);
    }

    // Get unassigned tickets
    public Page<TicketDto> getUnassignedTickets(Pageable pageable) {
        return ticketRepository.findByAssignedToIdIsNull(pageable).map(this::convertToDto);
    }

    // Search tickets with filters
    public Page<TicketDto> searchTickets(Ticket.Status status, 
                                        Ticket.Priority priority, 
                                        String assignedToId, 
                                        String customerId, 
                                        Pageable pageable) {
        return ticketRepository.findTicketsWithFilters(status, priority, assignedToId, customerId, pageable)
                .map(this::convertToDto);
    }

    // Text search in tickets
    public Page<TicketDto> searchTicketsByText(String searchText, Pageable pageable) {
        return ticketRepository.findByTextSearch(searchText, pageable).map(this::convertToDto);
    }

    // Update ticket
    @Transactional
    public TicketDto updateTicket(String id, UpdateTicketDto updateDto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
       Ticket previousState = cloneTicket(ticket);
       
        boolean hasChanges = false;
        // Update fields only if they are provided
        if (updateDto.getTitle() != null) {
            ticket.setTitle(updateDto.getTitle());
             hasChanges = true;
        }
        if (updateDto.getDescription() != null) {
            ticket.setDescription(updateDto.getDescription());
            hasChanges = true;
        }
        if (updateDto.getStatus() != null) {
            ticket.setStatus(updateDto.getStatus());
             hasChanges = true;
        }
        if (updateDto.getPriority() != null) {
            ticket.setPriority(updateDto.getPriority());
            hasChanges = true;
        }
        if (updateDto.getType() != null) {
            ticket.setType(updateDto.getType());
             hasChanges = true;
        }
        if (updateDto.getTags() != null) {
            ticket.setTags(updateDto.getTags());
        }
        if(hasChanges){
                ticket = ticketRepository.save(ticket);
                log.info("Publishing ticket.updated event for ticket: {}", ticket.getTicketNumber());
            eventPublisher.publishEvent(
                new TicketEvent(this, ticket, WebhookEventType.TICKET_UPDATED, previousState, "system")
            );
        }
        return convertToDto(ticket);
    }

    // Assign ticket to agent
    @Transactional
    public TicketDto assignTicket(String ticketId, String agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
            
        Ticket previousState = cloneTicket(ticket);
        String previousAssignee = ticket.getAssignedToName();
        if (agentId == null) {
            // Unassign ticket
            ticket.setAssignedToId(null);
            ticket.setAssignedToName(null);
             // Add system message
            TicketMessage systemMessage = TicketMessage.createSystemMessage(
                "Ticket unassigned from " + (previousAssignee != null ? previousAssignee : "agent")
            );
            ticket.addMessage(systemMessage);
            ticket = ticketRepository.save(ticket);
              log.info("Publishing ticket.unassigned event for ticket: {}", ticket.getTicketNumber());
            eventPublisher.publishEvent(
                new TicketEvent(this, ticket, WebhookEventType.TICKET_UNASSIGNED, previousState, "system")
            );
        } else {
            // Assign to agent
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new RuntimeException("Agent not found with id: " + agentId));

            // Verify the user is an agent, manager, or admin
            if (!isAgentRole(agent.getRole())) {
                throw new RuntimeException("User is not authorized to handle tickets");
            }

            ticket.setAssignedToId(agent.getId());
            ticket.setAssignedToName(agent.getName());
            // Add system message
            String messageContent = previousAssignee != null ?
                "Ticket reassigned from " + previousAssignee + " to " + agent.getName() :
                "Ticket assigned to " + agent.getName();
            
            TicketMessage systemMessage = TicketMessage.createSystemMessage(messageContent);
            ticket.addMessage(systemMessage);
                    ticket = ticketRepository.save(ticket);

            log.info("Publishing ticket.assigned event for ticket: {}", ticket.getTicketNumber());
            eventPublisher.publishEvent(
                new TicketEvent(this, ticket, WebhookEventType.TICKET_ASSIGNED, previousState, agent.getEmail())
            );
        }

        return convertToDto(ticket);
    }

    // Change ticket status
    @Transactional
    public TicketDto changeTicketStatus(String ticketId, Ticket.Status newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
                
        Ticket previousState = cloneTicket(ticket);
        Ticket.Status oldStatus = ticket.getStatus();
         if (oldStatus == newStatus) {
            // No change, return as is
            return convertToDto(ticket);
        }
        ticket.setStatus(newStatus);
         TicketMessage systemMessage = TicketMessage.createSystemMessage(
            "Ticket status changed from " + oldStatus + " to " + newStatus
        );
        ticket.addMessage(systemMessage);

        ticket = ticketRepository.save(ticket);
         String statusSpecificEvent = WebhookEventType.getEventTypeFromStatus(newStatus);
        
        log.info("Publishing {} event for ticket: {}", statusSpecificEvent, ticket.getTicketNumber());
        
        // Publish status-specific event (e.g., ticket.closed, ticket.in_progress)
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, statusSpecificEvent, previousState, "system")
        );
        
        // Also publish generic status changed event
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, WebhookEventType.TICKET_STATUS_CHANGED, previousState, "system")
        );
        
        // Special handling for reopened tickets
        if (oldStatus == Ticket.Status.CLOSED && newStatus == Ticket.Status.OPEN) {
            log.info("Publishing ticket.reopened event for ticket: {}", ticket.getTicketNumber());
            eventPublisher.publishEvent(
                new TicketEvent(this, ticket, WebhookEventType.TICKET_REOPENED, previousState, "system")
            );
        }
        return convertToDto(ticket);
    }

        private boolean isAgentRole(User.Role role) {
        return role == User.Role.AGENT || role == User.Role.MANAGER || role == User.Role.ADMIN;
    }

    // Get ticket statistics
    public TicketStatsDto getTicketStatistics() {
        TicketStatsDto stats = new TicketStatsDto();
        
        stats.setTotalTickets(ticketRepository.count());
        stats.setOpenTickets(ticketRepository.countByStatus(Ticket.Status.OPEN));
        stats.setInProgressTickets(ticketRepository.countByStatus(Ticket.Status.IN_PROGRESS));
        stats.setResolvedTickets(ticketRepository.countByStatus(Ticket.Status.RESOLVED));
        stats.setClosedTickets(ticketRepository.countByStatus(Ticket.Status.CLOSED));
        stats.setAssignedTickets(ticketRepository.countByAssignedToIdIsNotNull());
        stats.setUnassignedTickets(ticketRepository.countByAssignedToIdIsNull());

        return stats;
    }
    @Transactional
     public void deleteTicket(String ticketId, String deletedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        
        ticketRepository.delete(ticket);
        
        // 🔔 PUBLISH WEBHOOK EVENT - Ticket Deleted
        log.info("Publishing ticket.deleted event for ticket: {}", ticket.getTicketNumber());
        eventPublisher.publishEvent(
            new TicketEvent(this, ticket, WebhookEventType.TICKET_DELETED, deletedBy)
        );
    }

    // Helper methods
 

    private TicketDto convertToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        dto.setType(ticket.getType());
        dto.setCustomerId(ticket.getCustomerId());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setCustomerEmail(ticket.getCustomerEmail());
        dto.setAssignedToId(ticket.getAssignedToId());
        dto.setAssignedToName(ticket.getAssignedToName());
        dto.setTags(ticket.getTags());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
     
     
        return dto;
    }
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

    public TicketDto getTicketWithMessages(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        TicketDto dto = convertToDto(ticket);
        
        // Include messages in the response
        if (ticket.getMessages() != null && !ticket.getMessages().isEmpty()) {
            List<TicketMessageDto> messageDtos = ticket.getMessages().stream()
                    .map(this::convertMessageToDto)
                    .collect(Collectors.toList());
            dto.setMessages(messageDtos);
        }
        
        return dto;
    }

     public void addSystemMessage(String ticketId, String content) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));

        TicketMessage systemMessage = TicketMessage.createSystemMessage(content);
        ticket.addMessage(systemMessage);
        ticketRepository.save(ticket);
    }

     private Ticket cloneTicket(Ticket ticket) {
        Ticket clone = new Ticket(
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getCustomerId(),
            ticket.getCustomerName(),
            ticket.getCustomerEmail()
        );
        clone.setId(ticket.getId());
        clone.setTicketNumber(ticket.getTicketNumber());
        clone.setStatus(ticket.getStatus());
        clone.setPriority(ticket.getPriority());
        clone.setType(ticket.getType());
        clone.setAssignedToId(ticket.getAssignedToId());
        clone.setAssignedToName(ticket.getAssignedToName());
        clone.setTags(ticket.getTags() != null ? new java.util.HashSet<>(ticket.getTags()) : null);
        clone.setCreatedAt(ticket.getCreatedAt());
        clone.setUpdatedAt(ticket.getUpdatedAt());
        clone.setMessageCount(ticket.getMessageCount());
        clone.setLastMessageAt(ticket.getLastMessageAt());
        clone.setLastMessageBy(ticket.getLastMessageBy());
        return clone;
    }
}


