package com.example.training.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    
    private Long userId;
    private String username;
    private String phone;
    private String role;
    private String wechatOpenid;
    private String wechatUnionid;
    private Long profileId;
    private String profileType;
    
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }
    
    public boolean isParent() {
        return "PARENT".equals(role);
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getWechatOpenid() { return wechatOpenid; }
    public void setWechatOpenid(String wechatOpenid) { this.wechatOpenid = wechatOpenid; }
    public String getWechatUnionid() { return wechatUnionid; }
    public void setWechatUnionid(String wechatUnionid) { this.wechatUnionid = wechatUnionid; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public String getProfileType() { return profileType; }
    public void setProfileType(String profileType) { this.profileType = profileType; }
}
