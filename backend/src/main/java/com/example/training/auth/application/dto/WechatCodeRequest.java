package com.example.training.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WechatCodeRequest {

    @NotBlank(message = "微信code不能为空")
    private String code;

    private String phoneEncryptedData;

    private String phoneIv;
}
