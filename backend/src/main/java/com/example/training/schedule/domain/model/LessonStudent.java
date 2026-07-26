package com.example.training.schedule.domain.model;

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
public class LessonStudent {
    
    private Long id;
    private Long lessonId;
    private Long studentId;
    private String attendanceStatus;
    private LocalDateTime signInAt;
    private BigDecimal consumeHours;
    private Boolean shouldConsume;
    private Long hourTransactionId;
    private String attendanceRemark;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public boolean isPresent() {
        return "PRESENT".equals(attendanceStatus);
    }
    
    public boolean isLeave() {
        return "LEAVE".equals(attendanceStatus);
    }
    
    public boolean isAbsent() {
        return "ABSENT".equals(attendanceStatus);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(attendanceStatus);
    }
}
