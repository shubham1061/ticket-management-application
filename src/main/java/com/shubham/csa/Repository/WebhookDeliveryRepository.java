package com.shubham.csa.Repository;

import com.shubham.csa.entity.WebhookDelivery;
import com.shubham.csa.entity.WebhookDelivery.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookDeliveryRepository extends MongoRepository<WebhookDelivery, String> {
    
    // Find deliveries by webhook ID
    Page<WebhookDelivery> findByWebhookId(String webhookId, Pageable pageable);
    
    // Find deliveries by ticket ID
    Page<WebhookDelivery> findByTicketId(String ticketId, Pageable pageable);
    
    // Find deliveries by status
    List<WebhookDelivery> findByStatus(DeliveryStatus status);
    
    // Find pending deliveries that are ready for retry
    @Query("{ 'status': ?0, 'nextRetryAt': { $lte: ?1 } }")
    List<WebhookDelivery> findPendingRetries(DeliveryStatus status, LocalDateTime now);
    
    // Find failed deliveries for a webhook
    Page<WebhookDelivery> findByWebhookIdAndStatus(String webhookId, DeliveryStatus status, Pageable pageable);
    
    // Find recent deliveries for a webhook
    List<WebhookDelivery> findTop10ByWebhookIdOrderByCreatedAtDesc(String webhookId);
    
    // Count deliveries by webhook and status
    long countByWebhookIdAndStatus(String webhookId, DeliveryStatus status);
    
    // Find deliveries within date range
    List<WebhookDelivery> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Delete old deliveries (for cleanup)
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}