package com.example.training.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String username;
    private String role;
    private String phone;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
