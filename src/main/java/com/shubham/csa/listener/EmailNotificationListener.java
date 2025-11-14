package com.shubham.csa.listener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.shubham.csa.Repository.UserRepository;
import com.shubham.csa.Service.EmailService;
import com.shubham.csa.constants.WebhookEventType;
import com.shubham.csa.entity.Ticket;
import com.shubham.csa.entity.User;
import com.shubham.csa.event.TicketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationListener {

     private final EmailService emailService;
    private final UserRepository userRepository;
    
    /**
     * Listen to ticket created events
     * Send email to Admin and Manager
     */

    @EventListener
    @Async
    public void handleTicketCreated(TicketEvent event) {
        if (!WebhookEventType.TICKET_CREATED.equals(event.getEventType())) {
            return;
        }
        
        log.info("Processing ticket created email notification for: {}", event.getTicket().getTicketNumber());
        
        try {
            Ticket ticket = event.getTicket();
            
            // Find admin and manager users
            User admin = findAdmin();
            User manager = findManager();
            
            if (admin == null && manager == null) {
                log.warn("No admin or manager found to notify for ticket creation");
                return;
            }
            
            // Send email to admin and manager
            emailService.sendTicketCreatedEmail(ticket, admin, manager);
            
        } catch (Exception e) {
            log.error("Error sending ticket creation email notification", e);
        }
    }
    
    /**
     * Listen to ticket assigned events
     * Send email to the assignee
     */
    @EventListener
    @Async
    public void handleTicketAssigned(TicketEvent event) {
        if (!WebhookEventType.TICKET_ASSIGNED.equals(event.getEventType())) {
            return;
        }
        
        log.info("Processing ticket assignment email notification for: {}", event.getTicket().getTicketNumber());
        
        try {
            Ticket ticket = event.getTicket();
            String assigneeId = ticket.getAssignedToId();
            
            if (assigneeId == null) {
                log.debug("Ticket has no assignee, skipping email");
                return;
            }
            
            // Find the assignee
            User assignee = userRepository.findById(assigneeId).orElse(null);
            
            if (assignee == null) {
                log.warn("Assignee not found with ID: {}", assigneeId);
                return;
            }
            
            // Send email to assignee
            emailService.sendTicketAssignedEmail(ticket, assignee);
            
        } catch (Exception e) {
            log.error("Error sending ticket assignment email notification", e);
        }
    }
    
    /**
     * Find the first admin user
     */
    private User findAdmin() {
        List<User> admins = userRepository.findByRole(User.Role.ADMIN);
        if (admins.isEmpty()) {
            log.warn("No admin user found in the system");
            return null;
        }
        return admins.get(0);
    }
    
    /**
     * Find the first manager user
     */
    private User findManager() {
        List<User> managers = userRepository.findByRole(User.Role.MANAGER);
        if (managers.isEmpty()) {
            log.warn("No manager user found in the system");
            return null;
        }
        return managers.get(0);
    }

}
