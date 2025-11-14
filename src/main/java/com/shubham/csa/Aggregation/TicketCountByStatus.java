package com.shubham.csa.Aggregation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.shubham.csa.entity.Ticket;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCountByStatus {
    private Ticket.Status status;
    private long count;
}
