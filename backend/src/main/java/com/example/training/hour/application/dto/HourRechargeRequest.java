package com.example.training.hour.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourRechargeRequest {
    
    @NotNull(message = "学员ID不能为空")
    private Long studentId;
    
    @NotNull(message = "充值数量不能为空")
    @Positive(message = "充值数量必须大于0")
    private BigDecimal amount;
    
    private String reason;
}
