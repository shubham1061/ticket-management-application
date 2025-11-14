package com.shubham.csa.Service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.shubham.csa.entity.Ticket;
import com.shubham.csa.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final SpringTemplateEngine templateEngine;
    @Autowired
    private final JavaMailSender mailSender;
 
    /**
     * Send email when ticket is created - to Admin & Manager
     */
    @Value("${spring.mail.from:noreply@support.com}")
    private String fromEmail;
    
    @Async
    public void sendTicketCreatedEmail(Ticket ticket, User admin, User manager) {
        try {
            log.info("Sending ticket creation email for ticket: {}", ticket.getTicketNumber());
            
            String subject = "New Support Ticket Created - " + ticket.getTicketNumber();
            
            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("ticketNumber", ticket.getTicketNumber());
            variables.put("title", ticket.getTitle());
            variables.put("description", ticket.getDescription());
            variables.put("priority", ticket.getPriority().toString());
            variables.put("status", ticket.getStatus().toString());
            variables.put("customerName", ticket.getCustomerName());
            variables.put("customerEmail", ticket.getCustomerEmail());
            variables.put("createdAt", ticket.getCreatedAt());
            
            // Send to Admin
            if (admin != null && admin.getEmail() != null) {
                variables.put("recipientName", admin.getName());
                variables.put("recipientRole", "Administrator");
                sendHtmlEmailWithReplyTo(admin.getEmail(), subject, "ticket-created", variables,ticket.getCustomerEmail(),  // ← REPLY-TO: Customer email
                    ticket.getCustomerName());
                log.info("Ticket creation email sent to admin: {}", admin.getEmail());
            }
            
            // Send to Manager
            if (manager != null && manager.getEmail() != null) {
                variables.put("recipientName", manager.getName());
                variables.put("recipientRole", "Manager");
                sendHtmlEmail(manager.getEmail(), subject, "ticket-created", variables);
                log.info("Ticket creation email sent to manager: {}", manager.getEmail());
            }
            
        } catch (Exception e) {
            log.error("Failed to send ticket creation email for ticket: {}", ticket.getTicketNumber(), e);
        }
    }
    
    /**
     * Send email when ticket is assigned to an agent
     */
    @Async
    public void sendTicketAssignedEmail(Ticket ticket, User assignee) {
        try {
            log.info("Sending ticket assignment email to agent: {}", assignee.getEmail());
            
            String subject = "Ticket Assigned to You - " + ticket.getTicketNumber();
            
            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("agentName", assignee.getName());
            variables.put("ticketNumber", ticket.getTicketNumber());
            variables.put("title", ticket.getTitle());
            variables.put("description", ticket.getDescription());
            variables.put("priority", ticket.getPriority().toString());
            variables.put("status", ticket.getStatus().toString());
            variables.put("customerName", ticket.getCustomerName()); 
            variables.put("customerEmail", ticket.getCustomerEmail());
            variables.put("createdAt", ticket.getCreatedAt());
            
            sendHtmlEmail(assignee.getEmail(), subject, "ticket-assigned", variables);
            log.info("Ticket assignment email sent successfully to: {}", assignee.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send ticket assignment email to: {}", assignee.getEmail(), e);
        }
    }
    
    /**
     * Send simple text email
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        //    if (!isEmailAvailable()) {
        //     log.info("Skipping simple email to: {} (email not available)", to);
        //     return;
        // }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@support.com"); // Configure this in properties
            
            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }
    
    /**
     * Send HTML email using Thymeleaf template
     * @throws UnsupportedEncodingException 
     */
     @Async
    public void sendHtmlEmailWithReplyTo(String to, String subject, String templateName, 
                                          Map<String, Object> variables,
                                          String replyToEmail, String replyToName) throws MessagingException, UnsupportedEncodingException {
        // if (!isEmailAvailable()) {
        //     log.info("Skipping HTML email to: {} (email not available)", to);
        //     return;
        // }                                    
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // FROM: Your system email
            helper.setFrom(fromEmail);
            
            // TO: Recipient (admin, manager, or agent)
            helper.setTo(to);
            
            // REPLY-TO: Customer email (so replies go to customer)
            if (replyToEmail != null && !replyToEmail.isEmpty()) {
                helper.setReplyTo(replyToEmail, replyToName);
                log.info("Set Reply-To: {} <{}>", replyToName, replyToEmail);
            }
            
            helper.setSubject(subject);
            
            // Process template
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(templateName, context);
            
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("HTML email sent - FROM: {} | TO: {} | REPLY-TO: {}", 
                fromEmail, to, replyToEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
        }
    }
    
     @Async
    public void sendHtmlEmail(String to, String subject, String templateName, 
                              Map<String, Object> variables) throws UnsupportedEncodingException, MessagingException {
        sendHtmlEmailWithReplyTo(to, subject, templateName, variables, null, null);
    }
}
