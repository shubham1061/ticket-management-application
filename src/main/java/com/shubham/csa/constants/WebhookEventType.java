package com.shubham.csa.constants;

import com.shubham.csa.entity.Ticket;
import com.shubham.csa.entity.TicketMessage;

public class WebhookEventType {
    public static final String TICKET_CREATED = "ticket.created";
    public static final String TICKET_UPDATED = "ticket.updated";
    public static final String TICKET_DELETED = "ticket.deleted";
    
    // Ticket status events
    public static final String TICKET_OPENED = "ticket.opened";
    public static final String TICKET_IN_PROGRESS = "ticket.in_progress";
    public static final String TICKET_RESOLVED = "ticket.resolved";
    public static final String TICKET_CLOSED = "ticket.closed";
    public static final String TICKET_REOPENED = "ticket.reopened";
    public static final String TICKET_STATUS_CHANGED = "ticket.status_changed";
    // Ticket assignment events
    public static final String TICKET_ASSIGNED = "ticket.assigned";
    public static final String TICKET_UNASSIGNED = "ticket.unassigned";
    
    // Ticket priority events
    public static final String TICKET_PRIORITY_CHANGED = "ticket.priority_changed";
    
    // Message events
    public static final String TICKET_MESSAGE_ADDED = "ticket.message.added";
    public static final String TICKET_COMMENT_ADDED = "ticket.comment.added";
    public static final String TICKET_INTERNAL_NOTE_ADDED = "ticket.internal_note.added";
    public static final String TICKET_MESSAGE_DELETED = "ticket.message.deleted";
    public static final String TICKET_MESSAGE_UPDATED = "ticket.message.updated";
    // User events (optional)
    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";
    
    private WebhookEventType() {
        // Utility class - prevent instantiation
    }
    
    public static String[] getAllTicketEventTypes() {
        return new String[] {
            TICKET_CREATED,
            TICKET_UPDATED,
            TICKET_DELETED,
            TICKET_OPENED,
            TICKET_IN_PROGRESS,
            TICKET_RESOLVED,
            TICKET_CLOSED,
            TICKET_REOPENED,
            TICKET_ASSIGNED,
            TICKET_UNASSIGNED,
            TICKET_PRIORITY_CHANGED,
            TICKET_MESSAGE_ADDED,
            TICKET_COMMENT_ADDED,
            TICKET_INTERNAL_NOTE_ADDED,
            TICKET_MESSAGE_DELETED,
            TICKET_MESSAGE_UPDATED

        };
    }
    
    public static String[] getAllUserEventTypes() {
        return new String[] {
            USER_CREATED,
            USER_UPDATED,
            USER_DELETED
        };
    }
    
    public static String[] getAllEventTypes() {
        String[] ticketEvents = getAllTicketEventTypes();
        String[] userEvents = getAllUserEventTypes();
        String[] allEvents = new String[ticketEvents.length + userEvents.length];
        System.arraycopy(ticketEvents, 0, allEvents, 0, ticketEvents.length);
        System.arraycopy(userEvents, 0, allEvents, ticketEvents.length, userEvents.length);
        return allEvents;
    }
    
    // Helper method to determine event type from ticket status change
    public static String getEventTypeFromStatus(Ticket.Status status) {
        switch (status) {
            case OPEN:
                return TICKET_OPENED;
            case IN_PROGRESS:
                return TICKET_IN_PROGRESS;
            case RESOLVED:
                return TICKET_RESOLVED;
            case CLOSED:
                return TICKET_CLOSED;
            default:
                return TICKET_UPDATED;
        }
    }
     public static String getEventTypeFromMessageStatus(TicketMessage.status status){
        switch (status) {
            case ADDED:
                return TICKET_MESSAGE_ADDED;
            case DELETED:
                return TICKET_MESSAGE_DELETED;
           default:
                 return TICKET_MESSAGE_UPDATED;
        }
    }
    
}
