package com.shubham.csa.Aggregation;
import com.shubham.csa.entity.Ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AverageResolutionTime {
  private Ticket.Priority priority;
    private double avgResolutionTimeMs;
    private long ticketCount;
    
    // Helper method to get hours
    public double getAvgResolutionTimeHours() {
        return avgResolutionTimeMs / (1000.0 * 60 * 60);
    }
}
