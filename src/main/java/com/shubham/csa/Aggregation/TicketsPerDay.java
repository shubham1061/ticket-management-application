package com.shubham.csa.Aggregation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketsPerDay {
  private int year;
    private int month;
    private int day;
    private long count;
}
