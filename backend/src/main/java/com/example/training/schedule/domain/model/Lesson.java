package com.example.training.schedule.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    
    private Long id;
    private String lessonNo;
    private Long scheduleSeriesId;
    private Long courseId;
    private Long classGroupId;
    private Long teacherId;
    private LocalDate lessonDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String location;
    private String cancelReason;
    private LocalDateTime completedAt;
    private Long completedBy;
    private String sourceType;
    private BigDecimal consumeHours;
    private Integer version;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
}
