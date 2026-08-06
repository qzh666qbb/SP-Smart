package com.example.training.hour.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentPackageRecord {

    private Long id;
    private Long studentId;
    private Long packageId;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private BigDecimal totalHours;
    private BigDecimal remainingHours;
    private BigDecimal consumedHours;
    private BigDecimal refundedHours;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
