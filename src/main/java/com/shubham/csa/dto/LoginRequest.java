package com.shubham.csa.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
 @NotBlank(message = "Email is required")
    @Email(message = "Valid email format required")
    private String Email;

    // In real app, you'd have password here
    // For demo, we'll use API key or simplified auth
    private String password;
}
