package com.example.training.course.domain.model;

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
public class ClassGroup {
    
    private Long id;
    private String classCode;
    private String className;
    private Long courseId;
    private Long primaryTeacherId;
    private Integer maxStudents;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
