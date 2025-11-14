package com.shubham.csa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookResponse {
    private String id;
    private String tenantId;
    private String name;
    private String description;
    private String url;
    private String secret;
    private Set<String> eventTypes;
    private Map<String, String> customHeaders;
    private int maxRetries;
    private int retryDelaySeconds;
    private boolean active;
    
    // Statistics
    private long totalDeliveries;
    private long successfulDeliveries;
    private long failedDeliveries;
    private double successRate;
    private LocalDateTime lastDeliveryAt;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}