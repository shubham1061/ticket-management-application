package com.shubham.csa.Repository;

import com.shubham.csa.entity.Webhook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookRepository extends MongoRepository<Webhook, String> {
    
    // Find all active webhooks
    List<Webhook> findByActiveTrue();
    
    // Find active webhooks by tenant
    List<Webhook> findByTenantIdAndActiveTrue(String tenantId);
    
    // Find webhooks subscribed to a specific event type
    List<Webhook> findByActiveTrueAndEventTypesContaining(String eventType);
    
    // Find webhooks by tenant and event type
    List<Webhook> findByTenantIdAndActiveTrueAndEventTypesContaining(String tenantId, String eventType);
    
    // Find by URL (for duplicate checking)
    Optional<Webhook> findByUrl(String url);
    
    // Find all webhooks by tenant
    List<Webhook> findByTenantId(String tenantId);
    
    // Find by name (case-insensitive)
    Optional<Webhook> findByTenantIdAndNameIgnoreCase(String tenantId, String name);
}