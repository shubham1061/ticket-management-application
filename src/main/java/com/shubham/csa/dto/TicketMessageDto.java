package com.shubham.csa.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.shubham.csa.entity.TicketMessage;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;






@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageDto {
     private String id;
    private String authorId;
    private String authorName;
    private String authorEmail;
    private String authorRole;
    private String content;
    private TicketMessage.MessageType messageType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean edited;
    private List<String> attachmentIds;
    private int likes;
}
