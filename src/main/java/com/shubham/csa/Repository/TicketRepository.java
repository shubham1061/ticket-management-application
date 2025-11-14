package com.shubham.csa.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

// importorg.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.shubham.csa.entity.Ticket;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String>{
       Optional<Ticket> findByTicketNumber(String ticketNumber);

    // Find tickets by customer
    Page<Ticket> findByCustomerId(String customerId, Pageable pageable);
    List<Ticket> findByCustomerId(String customerId);

    // Find tickets assigned to an agent
    Page<Ticket> findByAssignedToId(String assignedToId, org.springframework.data.domain.Pageable pageable);
    List<Ticket> findByAssignedToId(String assignedToId);

    // Find tickets by status
    Page<Ticket> findByStatus(Ticket.Status status, Pageable pageable);
    List<Ticket> findByStatus(Ticket.Status status);

    // Find tickets by priority
    Page<Ticket> findByPriority(Ticket.Priority priority, Pageable pageable);

    // Find unassigned tickets
    Page<Ticket> findByAssignedToIdIsNull(Pageable pageable);
    List<Ticket> findByAssignedToIdIsNull();


    // Complex search with multiple filters
    
    @Query("{ " +
           "$and: [ " +
           "  { $or: [ { status: { $exists: false } }, { status: ?0 } ] }, " +
           "  { $or: [ { priority: { $exists: false } }, { priority: ?1 } ] }, " +
           "  { $or: [ { assignedToId: { $exists: false } }, { assignedToId: ?2 } ] }, " +
           "  { $or: [ { customerId: { $exists: false } }, { customerId: ?3 } ] } " +
           "] " +
           "}")
    Page<Ticket> findTicketsWithFilters(Ticket.Status status, 
                                       Ticket.Priority priority, 
                                       String assignedToId, 
                                       String customerId, 
                                       Pageable pageable);

    // Text search in title and description
    @Query("{'$text': {'$search': ?0}}")
    Page<Ticket> findByTextSearch(String searchText, Pageable pageable);

    // Count tickets by status
    long countByStatus(Ticket.Status status);

    // Count tickets by priority
    long countByPriority(Ticket.Priority priority);

    // Count tickets by customer
    long countByCustomerId(String customerId);

    // Count assigned vs unassigned tickets
    long countByAssignedToIdIsNotNull();
    long countByAssignedToIdIsNull();


      @Query("{ $text: { $search: ?0 } }")
    Page<Ticket> searchByText(String searchText, Pageable pageable);
    
    /**
     * Full-text search with tenant isolation
     */
    @Query("{ tenantId: ?0, $text: { $search: ?1 } }")
    Page<Ticket> searchByTextAndTenant(String tenantId, String searchText, Pageable pageable);
    
    /**
     * Text search with status filter
     */
    @Query("{ tenantId: ?0, status: ?1, $text: { $search: ?2 } }")
    Page<Ticket> searchByTextAndTenantAndStatus(String tenantId, Ticket.Status status, 
                                                 String searchText, Pageable pageable);
    
    /**
     * Advanced text search with score sorting
     * Returns results sorted by relevance
     */
    @Query(value = "{ tenantId: ?0, $text: { $search: ?1 } }", 
           sort = "{ score: { $meta: 'textScore' } }")
    Page<Ticket> searchByTextWithScore(String tenantId, String searchText, Pageable pageable);
    
    @Query("{ " +
       "$and: [ " +
       "  { $or: [ { 'status': null }, { 'status': ?0 } ] }, " +
       "  { $or: [ { 'priority': null  }, { 'priority': ?1 } ] }, " +
       "  { $or: [ { 'assignedToId': null }, { 'assignedToId': ?2 } ] }, " +
       "  { $or: [ { 'customerId': null }, { 'customerId': ?3 } ] } " +
       "] " +
       "}")
List<Ticket> findTicketsWithFiltersForExport(Ticket.Status status, 
                                              Ticket.Priority priority, 
                                              String assignedToId, 
                                              String customerId);
   
}



