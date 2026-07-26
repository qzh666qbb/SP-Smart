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
}
