package com.example.training.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginRequest {
    
    @NotBlank(message = "微信code不能为空")
    private String code;

    @NotBlank(message = "role不能为空")
    @Pattern(regexp = "TEACHER|PARENT", message = "role必须是TEACHER或PARENT")
    private String role;
    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;
    @Size(max = 500, message = "头像地址不能超过500个字符")
    private String avatarUrl;

    public String getCode() { return code; }    public void setCode(String code) { this.code = code; }
    public String getRole() { return role; }    public void setRole(String role) { this.role = role; }
    public String getNickname() { return nickname; }    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
