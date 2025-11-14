package com.shubham.csa.dto;

import com.shubham.csa.entity.WebhookDelivery.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookDeliveryResponse {
    private String id;
    private String webhookId;
    private String webhookName;
    private String webhookUrl;
    private String eventType;
    private String ticketId;
    private String ticketNumber;
    private DeliveryStatus status;
    private int attemptCount;
    private int maxAttempts;
    private Integer responseCode;
    private String responseBody;
    private String errorMessage;
    private Long responseTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime nextRetryAt;
    private LocalDateTime deliveredAt;
}