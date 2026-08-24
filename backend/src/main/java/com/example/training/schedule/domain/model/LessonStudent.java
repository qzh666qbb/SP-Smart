package com.example.training.schedule.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Integer rating;
    private String ratingComment;
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

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getLessonId() { return lessonId; }    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public Long getStudentId() { return studentId; }    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getAttendanceStatus() { return attendanceStatus; }    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public LocalDateTime getSignInAt() { return signInAt; }    public void setSignInAt(LocalDateTime signInAt) { this.signInAt = signInAt; }
    public BigDecimal getConsumeHours() { return consumeHours; }    public void setConsumeHours(BigDecimal consumeHours) { this.consumeHours = consumeHours; }
    public Boolean getShouldConsume() { return shouldConsume; }    public void setShouldConsume(Boolean shouldConsume) { this.shouldConsume = shouldConsume; }
    public Long getHourTransactionId() { return hourTransactionId; }    public void setHourTransactionId(Long hourTransactionId) { this.hourTransactionId = hourTransactionId; }
    public String getAttendanceRemark() { return attendanceRemark; }    public void setAttendanceRemark(String attendanceRemark) { this.attendanceRemark = attendanceRemark; }
    public Long getConfirmedBy() { return confirmedBy; }    public void setConfirmedBy(Long confirmedBy) { this.confirmedBy = confirmedBy; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public Integer getRating() { return rating; }    public void setRating(Integer rating) { this.rating = rating; }
    public String getRatingComment() { return ratingComment; }    public void setRatingComment(String ratingComment) { this.ratingComment = ratingComment; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
