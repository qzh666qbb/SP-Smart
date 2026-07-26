package com.example.training.physical.domain.model;

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
public class PhysicalScoreDetail {
    
    private Long id;
    private Long recordId;
    private Long itemId;
    private BigDecimal rawValue;
    private BigDecimal convertedScore;
    private String level;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
