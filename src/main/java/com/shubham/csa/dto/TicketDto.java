package com.shubham.csa.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.shubham.csa.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {

    private String id;
    private String ticketNumber;
    private String title;
    private String description;
    private Ticket.Status status;
    private Ticket.Priority priority;
    private Ticket.Type type;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String assignedToId;
    private String assignedToName;
    private Set<String> tags;
    private int messageCount;
    private LocalDateTime lastMessageAt;
    private String lastMessageBy;
    private List<TicketMessageDto> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
