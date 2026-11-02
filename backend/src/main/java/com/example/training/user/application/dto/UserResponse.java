package com.example.training.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String username;
    private String role;
    private String phone;
    private String status;
    private Long profileId;
    private String profileName;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }    public void setUsername(String username) { this.username = username; }
    public String getRole() { return role; }    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getProfileId() { return profileId; }    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public String getProfileName() { return profileName; }    public void setProfileName(String profileName) { this.profileName = profileName; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
