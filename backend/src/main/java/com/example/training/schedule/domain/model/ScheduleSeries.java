package com.example.training.schedule.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ScheduleSeries {
    
    private Long id;
    private String seriesName;
    private Long courseId;
    private Long classGroupId;
    private java.util.List<Long> studentIds;
    private Long teacherId;
    private Long assistantTeacherId1;
    private Long assistantTeacherId2;
    private String recurrenceType;
    private String weekdays;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal consumeHours;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public String getScheduleName() { return seriesName; }
    public String getScheduleType() {
        if ("WEEKLY".equals(recurrenceType)) return "RECURRING";
        if ("NONE".equals(recurrenceType)) return "SINGLE";
        return recurrenceType;
    }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getSeriesName() { return seriesName; }    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getClassGroupId() { return classGroupId; }    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    public Long getTeacherId() { return teacherId; }    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getAssistantTeacherId1() { return assistantTeacherId1; }    public void setAssistantTeacherId1(Long assistantTeacherId1) { this.assistantTeacherId1 = assistantTeacherId1; }
    public Long getAssistantTeacherId2() { return assistantTeacherId2; }    public void setAssistantTeacherId2(Long assistantTeacherId2) { this.assistantTeacherId2 = assistantTeacherId2; }
    public String getRecurrenceType() { return recurrenceType; }    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }
    public String getWeekdays() { return weekdays; }    public void setWeekdays(String weekdays) { this.weekdays = weekdays; }
    public LocalDate getStartDate() { return startDate; }    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalTime getStartTime() { return startTime; }    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public BigDecimal getConsumeHours() { return consumeHours; }    public void setConsumeHours(BigDecimal consumeHours) { this.consumeHours = consumeHours; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
