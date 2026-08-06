package com.example.training.hour.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentHourAccount {
    
    private Long id;
    private Long studentId;
    private BigDecimal totalRecharged;
    private BigDecimal totalConsumed;
    private BigDecimal totalRefunded;
    private BigDecimal remainingHours;
    private BigDecimal paidHours;
    private BigDecimal giftHours;
    private BigDecimal experienceHours;
    private BigDecimal activityHours;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
