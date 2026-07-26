package com.example.training.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private String phone;
    private String wechatOpenid;
    private String wechatUnionid;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordUpdatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isEnabled() {
        return "ENABLED".equals(status);
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }
    
    public boolean isParent() {
        return "PARENT".equals(role);
    }
}
