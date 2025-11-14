package com.shubham.csa.Controller;
import com.shubham.csa.entity.Ticket;
import com.shubham.csa.Repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/search/text")
@RequiredArgsConstructor
public class TicketSearchController {
    
    private final TicketRepository ticketRepository;
    private static final String DEFAULT_TENANT = "default";
    
    /**
     * GET /api/tickets/search?q=login+issue&page=0&size=20
     * Full-text search on title and description
     */
    @GetMapping
    public ResponseEntity<Page<Ticket>> searchTickets(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> results = ticketRepository.searchByTextAndTenant(DEFAULT_TENANT, q, pageable);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * GET /api/tickets/search/by-status?q=login&status=OPEN
     * Text search with status filter
     */
    @GetMapping("/by-status")
    public ResponseEntity<Page<Ticket>> searchByStatus(
            @RequestParam String q,
            @RequestParam Ticket.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> results = ticketRepository.searchByTextAndTenantAndStatus(
            DEFAULT_TENANT, status, q, pageable
        );
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * GET /api/tickets/search/relevant?q=cannot+login
     * Search with relevance scoring (most relevant first)
     */
    @GetMapping("/relevant")
    public ResponseEntity<Page<Ticket>> searchRelevant(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> results = ticketRepository.searchByTextWithScore(DEFAULT_TENANT, q, pageable);
        
        return ResponseEntity.ok(results);
    }
}
