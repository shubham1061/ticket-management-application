package com.shubham.csa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import java.util.Set;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookRequest {
    
    @NotBlank(message = "Webhook name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Webhook URL is required")
    @Pattern(regexp = "https?://.+", message = "URL must be a valid HTTP or HTTPS URL")
    private String url;
    
    @NotEmpty(message = "At least one event type must be selected")
    private Set<String> eventTypes;
    
    private Map<String, String> customHeaders;
    
    private Integer maxRetries;
    private Integer retryDelaySeconds;
    
    private boolean active = true;
}