package com.shubham.csa.entity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "webhooks")

@Builder
public class Webhook {
    @Id
    private String id;
    
    @Indexed
    private String tenantId = "default";
    
    // Webhook configuration
    private String name;
    private String description;
    
    @Indexed
    private String url;
    
    // Secret key for HMAC signature verification (will be generated)
    private String secret;
    
    // Events this webhook subscribes to
    private Set<String> eventTypes = new HashSet<>();
    
    @Indexed
    private boolean active = true;
    
    // Optional custom headers to send with webhook
    private Map<String, String> customHeaders = new HashMap<>();
    
    // Retry configuration
    private int maxRetries = 3;
    private int retryDelaySeconds = 60; // Initial retry delay
    
    // Statistics
    private long totalDeliveries = 0;
    private long successfulDeliveries = 0;
    private long failedDeliveries = 0;
    private LocalDateTime lastDeliveryAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void incrementDeliveryStats(boolean success) {
        this.totalDeliveries++;
        if (success) {
            this.successfulDeliveries++;
        } else {
            this.failedDeliveries++;
        }
        this.lastDeliveryAt = LocalDateTime.now();
    }
    
    public double getSuccessRate() {
        if (totalDeliveries == 0) return 0.0;
        return (double) successfulDeliveries / totalDeliveries * 100;
    }

}
