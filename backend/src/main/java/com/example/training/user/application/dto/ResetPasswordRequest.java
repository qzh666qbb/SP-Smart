package com.example.training.user.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResetPasswordRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String newPassword;
}
