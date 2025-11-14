package com.shubham.csa.scheduler;

import com.shubham.csa.Service.WebhookDeliveryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookScheduler {
    
    private final WebhookDeliveryService deliveryService;
    /**
     * Process pending webhook retries every minute
     */
    @Scheduled(fixedDelay = 60000) // 60 seconds
    public void processRetries() {
        log.debug("Processing pending webhook retries");
        try {
            deliveryService.processPendingRetries();
        } catch (Exception e) {
            log.error("Error processing webhook retries", e);
        }
    }

}