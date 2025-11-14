package com.shubham.csa.dto;

import java.time.LocalDateTime;

import com.shubham.csa.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
 private String id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format required")
    private String email;

    private User.Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
