package com.example.training.hour.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HourAccountResponse {

    private Long id;

    private Long studentId;

    private String studentName;

    private String studentNo;

    private String parentName;

    private String parentPhone;

    private BigDecimal totalHours;

    private BigDecimal usedHours;

    private BigDecimal remainingHours;

    private BigDecimal paidHours;

    private BigDecimal giftHours;

    private LocalDateTime createTime;
}
