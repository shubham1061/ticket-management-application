package com.shubham.csa.Aggregation;

import com.shubham.csa.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCountByPriority {
private Ticket.Priority priority;
    private long count;
}
