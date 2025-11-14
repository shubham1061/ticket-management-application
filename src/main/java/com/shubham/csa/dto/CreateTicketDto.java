package com.shubham.csa.dto;

import java.util.Set;

import com.shubham.csa.entity.Ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketDto {
@NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Priority is required")
    private Ticket.Priority priority = Ticket.Priority.MEDIUM;

    private Ticket.Type type = Ticket.Type.SUPPORT;

    // Customer info - either provide customerId or customerEmail
    private String customerId;
    private String customerEmail;
    private String customerName;

    private Set<String> tags;
}
