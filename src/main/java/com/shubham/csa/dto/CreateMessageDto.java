package com.shubham.csa.dto;

import java.util.List;

import com.shubham.csa.entity.TicketMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMessageDto {
 @NotBlank(message = "Message content is required")
    @Size(max = 5000, message = "Message content must not exceed 5000 characters")
    private String content;

    private TicketMessage.MessageType messageType = TicketMessage.MessageType.COMMENT;

   private List<String> attachmentIds;
}
