package com.shubham.csa.event;

import com.shubham.csa.entity.Ticket;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TicketEvent extends ApplicationEvent {
    
    private final Ticket ticket;
    private final String eventType;
    private final Ticket previousState; // For update events
    private final String triggeredBy;   // User who triggered the event
    
    // Constructor for create/delete events (no previous state)
    public TicketEvent(Object source, Ticket ticket, String eventType, String triggeredBy) {
        super(source);
        this.ticket = ticket;
        this.eventType = eventType;
        this.previousState = null;
        this.triggeredBy = triggeredBy;
    }
    
    // Constructor for update events (with previous state)
    public TicketEvent(Object source, Ticket ticket, String eventType, 
                       Ticket previousState, String triggeredBy) {
        super(source);
        this.ticket = ticket;
        this.eventType = eventType;
        this.previousState = previousState;
        this.triggeredBy = triggeredBy;
    }
    
    // Helper to check if it's an update event
    public boolean isUpdate() {
        return previousState != null;
    }
    
    // Helper to check if status changed
    public boolean isStatusChanged() {
        return isUpdate() && 
               previousState.getStatus() != ticket.getStatus();
    }
    
    // Helper to check if priority changed
    public boolean isPriorityChanged() {
        return isUpdate() && 
               previousState.getPriority() != ticket.getPriority();
    }
    
    // Helper to check if assigned
    public boolean isAssignmentChanged() {
        return isUpdate() && 
               !equals(previousState.getAssignedToId(), ticket.getAssignedToId());
    }
    
    private boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
