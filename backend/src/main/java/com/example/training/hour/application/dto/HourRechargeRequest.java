package com.example.training.hour.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    
    private String hourSource;

    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public BigDecimal getAmount() { return amount; }    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }    public void setReason(String reason) { this.reason = reason; }
    public String getHourSource() { return hourSource; }    public void setHourSource(String hourSource) { this.hourSource = hourSource; }
}
