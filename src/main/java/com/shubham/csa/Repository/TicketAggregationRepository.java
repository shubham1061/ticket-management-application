package com.shubham.csa.Repository;

import com.shubham.csa.Aggregation.*;
import com.shubham.csa.Aggregation.TicketCountByPriority;
import com.shubham.csa.Aggregation.TicketCountByStatus;
import com.shubham.csa.entity.Ticket;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
@RequiredArgsConstructor
public class TicketAggregationRepository {
    
    private final MongoTemplate mongoTemplate;
    
    /**
     * Aggregation 1: Count tickets by status
     */
    public List<TicketCountByStatus> countTicketsByStatus(String tenantId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)),
            group("status").count().as("count"),
            project("count").and("status").previousOperation(),
            sort(Sort.Direction.DESC, "count")
        );
        
        AggregationResults<TicketCountByStatus> results = mongoTemplate.aggregate(
            aggregation, "tickets", TicketCountByStatus.class
        );
        
        return results.getMappedResults();
    }
    
    /**
     * Aggregation 2: Count tickets by priority
     */
    public List<TicketCountByPriority> countTicketsByPriority(String tenantId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)),
            group("priority").count().as("count"),
            project("count").and("priority").previousOperation(),
            sort(Sort.Direction.DESC, "count")
        );
        
        AggregationResults<TicketCountByPriority> results = mongoTemplate.aggregate(
            aggregation, "tickets", TicketCountByPriority.class
        );
        
        return results.getMappedResults();
    }
    
    /**
     * Aggregation 3: Agent performance - tickets assigned per agent
     */
    public List<AgentTicketStats> getAgentTicketStats(String tenantId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)
                         .and("assignedToId").ne(null)),
            group("assignedToId", "assignedToName")
                .count().as("totalTickets")
                .sum(ConditionalOperators.when(Criteria.where("status").is(Ticket.Status.CLOSED))
                     .then(1).otherwise(0)).as("closedTickets")
                .sum(ConditionalOperators.when(Criteria.where("status").is(Ticket.Status.OPEN))
                     .then(1).otherwise(0)).as("openTickets")
                .sum(ConditionalOperators.when(Criteria.where("status").is(Ticket.Status.IN_PROGRESS))
                     .then(1).otherwise(0)).as("inProgressTickets"),
            project("totalTickets", "closedTickets", "openTickets", "inProgressTickets")
                .and("assignedToId").previousOperation()
                .and("assignedToName").previousOperation(),
            sort(Sort.Direction.DESC, "totalTickets")
        );
        
        AggregationResults<AgentTicketStats> results = mongoTemplate.aggregate(
            aggregation, "tickets", AgentTicketStats.class
        );
        
        return results.getMappedResults();
    }
    
    /**
     * Aggregation 4: Tickets created per day (last 30 days)
     */
    public List<TicketsPerDay> getTicketsCreatedPerDay(String tenantId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)
                         .and("createdAt").gte(startDate)),
            project()
                .and("createdAt").extractDayOfMonth().as("day")
                .and("createdAt").extractMonth().as("month")
                .and("createdAt").extractYear().as("year")
                .andInclude("status", "priority"),
            group("year", "month", "day")
                .count().as("count"),
            project("count")
                .and("year").previousOperation()
                .and("month").previousOperation()
                .and("day").previousOperation(),
            sort(Sort.Direction.ASC, "year", "month", "day")
        );
        
        AggregationResults<TicketsPerDay> results = mongoTemplate.aggregate(
            aggregation, "tickets", TicketsPerDay.class
        );
        
        return results.getMappedResults();
    }
    
    /**
     * Aggregation 5: Average resolution time by priority
     */
    public List<AverageResolutionTime> getAverageResolutionTimeByPriority(String tenantId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)
                         .and("status").is(Ticket.Status.CLOSED)),
            project()
                .and("priority").as("priority")
                .and(ArithmeticOperators.Subtract.valueOf("updatedAt").subtract("createdAt"))
                    .as("resolutionTimeMs"),
            group("priority")
                .avg("resolutionTimeMs").as("avgResolutionTimeMs")
                .count().as("ticketCount"),
            project("avgResolutionTimeMs", "ticketCount")
                .and("priority").previousOperation(),
            sort(Sort.Direction.ASC, "priority")
        );
        
        AggregationResults<AverageResolutionTime> results = mongoTemplate.aggregate(
            aggregation, "tickets", AverageResolutionTime.class
        );
        
        return results.getMappedResults();
    }
    
    /**
     * Aggregation 6: Customer ticket summary
     */
    public List<CustomerTicketSummary> getCustomerTicketSummary(String tenantId) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)),
            group("customerId", "customerName", "customerEmail")
                .count().as("totalTickets")
                .sum(ConditionalOperators.when(Criteria.where("status").is(Ticket.Status.OPEN))
                     .then(1).otherwise(0)).as("openTickets")
                .sum(ConditionalOperators.when(Criteria.where("status").is(Ticket.Status.CLOSED))
                     .then(1).otherwise(0)).as("closedTickets")
                .first("createdAt").as("firstTicketDate")
                .last("createdAt").as("lastTicketDate"),
            project("totalTickets", "openTickets", "closedTickets", "firstTicketDate", "lastTicketDate")
                .and("customerId").previousOperation()
                .and("customerName").previousOperation()
                .and("customerEmail").previousOperation(),
            sort(Sort.Direction.DESC, "totalTickets")
        );
        
        AggregationResults<CustomerTicketSummary> results = mongoTemplate.aggregate(
            aggregation, "tickets", CustomerTicketSummary.class
        );
        
        return results.getMappedResults();
    }
    
    /**
     * Aggregation 7: Tag popularity (most used tags)
     */
    public List<TagPopularity> getTagPopularity(String tenantId, int limit) {
        Aggregation aggregation = newAggregation(
            match(Criteria.where("tenantId").is(tenantId)),
            unwind("tags"),
            group("tags").count().as("count"),
            project("count").and("tag").previousOperation(),
            sort(Sort.Direction.DESC, "count"),
            limit(limit)
        );
        
        AggregationResults<TagPopularity> results = mongoTemplate.aggregate(
            aggregation, "tickets", TagPopularity.class
        );
        
        return results.getMappedResults();
    }
}
