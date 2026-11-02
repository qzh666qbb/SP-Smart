package com.example.training.auth.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WechatProfileUpdateRequest {

    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;

    @Size(max = 500, message = "头像地址不能超过500个字符")
    private String avatarUrl;
}
