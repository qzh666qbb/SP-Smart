package com.example.training.physical.domain.model;

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
public class PhysicalTestRecord {
    
    private Long id;
    private String recordNo;
    private Long batchId;
    private Long studentId;
    private LocalDate testDate;
    private Long testerId;
    private BigDecimal totalScore;
    private String overallLevel;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
