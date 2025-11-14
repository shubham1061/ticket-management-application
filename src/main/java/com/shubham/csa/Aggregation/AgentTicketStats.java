package com.shubham.csa.Aggregation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentTicketStats {
private String assignedToId;
    private String assignedToName;
    private long totalTickets;
    private long closedTickets;
    private long openTickets;
    private long inProgressTickets;
}
