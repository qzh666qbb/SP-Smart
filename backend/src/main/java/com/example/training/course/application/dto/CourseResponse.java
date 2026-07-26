package com.example.training.course.application.dto;

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
public class CourseResponse {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String courseType;
    private Integer defaultDurationMinutes;
    private BigDecimal defaultConsumeHours;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
