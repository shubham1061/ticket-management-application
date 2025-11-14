package com.shubham.csa.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")

public class User {
      public enum Role {
        CUSTOMER, AGENT, MANAGER, ADMIN, CLIENT
    }

    @Id
    private String id;

    @Indexed
    private String tenantId = "default"; // Start with single tenant

    @Indexed
    private String name;

    @Indexed
    private String email;

    @Indexed
    private Role role = Role.CUSTOMER;
    
    @Indexed
    private String password;
    @Indexed
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
