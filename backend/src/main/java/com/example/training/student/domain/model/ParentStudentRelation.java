package com.example.training.student.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentStudentRelation {
    
    private Long id;
    private Long parentId;
    private Long studentId;
    private String relationType;
    private Boolean isPrimary;
    private String bindStatus;
    private LocalDateTime boundAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
