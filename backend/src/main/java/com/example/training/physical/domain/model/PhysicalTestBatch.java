package com.example.training.physical.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalTestBatch {
    private Long id;
    private String batchName;
    private String batchCode;
    private Long templateId;
    private LocalDate testDate;
    private String location;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
}
