package com.example.training.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    
    @Size(max = 64, message = "用户名不能超过64个字符")
    private String username;
    
    @Size(min = 6, message = "密码至少6位")
    private String password;
    
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|TEACHER|PARENT)$", message = "角色必须是ADMIN、TEACHER或PARENT")
    private String role;
    
    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号格式不正确（留空或11位有效手机号）")
    private String phone;
    
    @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "状态必须是ENABLED或DISABLED")
    private String status;

    @Size(max = 64, message = "教师姓名不能超过64个字符")
    private String teacherName;

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = trimToNull(username); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password == null || password.isBlank() ? null : password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = trimToNull(role); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = trimToNull(phone); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = trimToNull(status) == null ? "ENABLED" : trimToNull(status); }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = trimToNull(teacherName); }
}
