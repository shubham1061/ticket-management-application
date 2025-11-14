package com.shubham.csa.Service;

import com.shubham.csa.dto.*;
import com.shubham.csa.Aggregation.*;
import com.shubham.csa.Repository.TicketAggregationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAnalyticsService {
    
    private final TicketAggregationRepository aggregationRepository;
    
    public List<TicketCountByStatus> getTicketCountByStatus(String tenantId) {
        log.info("Getting ticket count by status for tenant: {}", tenantId);
        return aggregationRepository.countTicketsByStatus(tenantId);
    }
    
    public List<TicketCountByPriority> getTicketCountByPriority(String tenantId) {
        log.info("Getting ticket count by priority for tenant: {}", tenantId);
        return aggregationRepository.countTicketsByPriority(tenantId);
    }
    
    public List<AgentTicketStats> getAgentPerformance(String tenantId) {
        log.info("Getting agent performance stats for tenant: {}", tenantId);
        return aggregationRepository.getAgentTicketStats(tenantId);
    }
    
    public List<TicketsPerDay> getTicketTrends(String tenantId, int days) {
        log.info("Getting ticket trends for last {} days for tenant: {}", days, tenantId);
        return aggregationRepository.getTicketsCreatedPerDay(tenantId, days);
    }
    
    public List<AverageResolutionTime> getResolutionTimeAnalysis(String tenantId) {
        log.info("Getting resolution time analysis for tenant: {}", tenantId);
        return aggregationRepository.getAverageResolutionTimeByPriority(tenantId);
    }
    
    public List<CustomerTicketSummary> getCustomerInsights(String tenantId) {
        log.info("Getting customer insights for tenant: {}", tenantId);
        return aggregationRepository.getCustomerTicketSummary(tenantId);
    }
    
    public List<TagPopularity> getPopularTags(String tenantId, int limit) {
        log.info("Getting top {} popular tags for tenant: {}", limit, tenantId);
        return aggregationRepository.getTagPopularity(tenantId, limit);
    }    
}
