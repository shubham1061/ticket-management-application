package com.shubham.csa.dto;

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
public class UpdateTicketDto {
private String title;
    private String description;
    private Ticket.Status status;
    private Ticket.Priority priority;
    private Ticket.Type type;
    private String assignedToId;
    private Set<String> tags;

}
