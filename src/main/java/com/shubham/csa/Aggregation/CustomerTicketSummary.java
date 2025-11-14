package com.shubham.csa.Aggregation;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTicketSummary {
    private String customerId;
    private String customerName;
    private String customerEmail;
    private long totalTickets;
    private long openTickets;
    private long closedTickets;
    private LocalDateTime firstTicketDate;
    private LocalDateTime lastTicketDate;

}
