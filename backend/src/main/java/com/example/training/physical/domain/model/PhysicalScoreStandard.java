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
public class PhysicalScoreStandard {

    private Long id;
    private Long itemId;
    private String gender;
    private String ageGroup;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer score;
    private String level;
    private BigDecimal scoreValue;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
