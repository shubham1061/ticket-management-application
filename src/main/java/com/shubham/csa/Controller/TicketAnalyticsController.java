package com.shubham.csa.Controller;
import com.shubham.csa.Aggregation.*;
import com.shubham.csa.Service.TicketAnalyticsService;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/analytics/tickets")
@RequiredArgsConstructor
public class TicketAnalyticsController {
    
    private final TicketAnalyticsService analyticsService;
    private static final String DEFAULT_TENANT = "default";
    
    /**
     * GET /api/analytics/tickets/by-status
     * Returns ticket count grouped by status
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<TicketCountByStatus>> getTicketsByStatus() {
        return ResponseEntity.ok(analyticsService.getTicketCountByStatus(DEFAULT_TENANT));
    }
    
    /**
     * GET /api/analytics/tickets/by-priority
     * Returns ticket count grouped by priority
     */
    @GetMapping("/by-priority")
    public ResponseEntity<List<TicketCountByPriority>> getTicketsByPriority() {
        return ResponseEntity.ok(analyticsService.getTicketCountByPriority(DEFAULT_TENANT));
    }
    
    /**
     * GET /api/analytics/tickets/agent-performance
     * Returns performance stats for all agents
     */
    @GetMapping("/agent-performance")
    public ResponseEntity<List<AgentTicketStats>> getAgentPerformance() {
        return ResponseEntity.ok(analyticsService.getAgentPerformance(DEFAULT_TENANT));
    }
    
    /**
     * GET /api/analytics/tickets/trends?days=30
     * Returns tickets created per day for last N days
     */
    @GetMapping("/trends")
    public ResponseEntity<List<TicketsPerDay>> getTicketTrends(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(analyticsService.getTicketTrends(DEFAULT_TENANT, days));
    }
    
    /**
     * GET /api/analytics/tickets/resolution-time
     * Returns average resolution time by priority
     */
    @GetMapping("/resolution-time")
    public ResponseEntity<List<AverageResolutionTime>> getResolutionTime() {
        return ResponseEntity.ok(analyticsService.getResolutionTimeAnalysis(DEFAULT_TENANT));
    }
    
    /**
     * GET /api/analytics/tickets/customer-insights
     * Returns ticket summary per customer
     */
    @GetMapping("/customer-insights")
    public ResponseEntity<List<CustomerTicketSummary>> getCustomerInsights() {
        return ResponseEntity.ok(analyticsService.getCustomerInsights(DEFAULT_TENANT));
    }
    
    /**
     * GET /api/analytics/tickets/popular-tags?limit=10
     * Returns most used tags
     */
    @GetMapping("/popular-tags")
    public ResponseEntity<List<TagPopularity>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getPopularTags(DEFAULT_TENANT, limit));
    }

}
