package com.shubham.csa.event;

import com.shubham.csa.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserEvent extends ApplicationEvent {
    
    private final User user;
    private final String eventType;
    private final String triggeredBy;
    
    public UserEvent(Object source, User user, String eventType, String triggeredBy) {
        super(source);
        this.user = user;
        this.eventType = eventType;
        this.triggeredBy = triggeredBy;
    }
}