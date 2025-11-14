package com.shubham.csa.dto;

import jakarta.validation.constraints.Size;

import com.shubham.csa.entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserDto {
   @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Valid email format required")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
    @NotNull(message = "Role is required")
    private User.Role role;
}
