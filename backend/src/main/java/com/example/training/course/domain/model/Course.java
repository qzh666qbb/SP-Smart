package com.example.training.course.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String courseType;
    private Integer defaultDurationMinutes;
    private java.math.BigDecimal defaultConsumeHours;
    private String description;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isEnabled() {
        return "ENABLED".equals(status);
    }
}
