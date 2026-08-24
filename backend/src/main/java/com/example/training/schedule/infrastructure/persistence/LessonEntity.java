package com.example.training.schedule.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("lesson")
public class LessonEntity {
    
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
    private Long createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getLessonNo() { return lessonNo; }    public void setLessonNo(String lessonNo) { this.lessonNo = lessonNo; }
    public Long getScheduleSeriesId() { return scheduleSeriesId; }    public void setScheduleSeriesId(Long scheduleSeriesId) { this.scheduleSeriesId = scheduleSeriesId; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getClassGroupId() { return classGroupId; }    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    public Long getTeacherId() { return teacherId; }    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public LocalDate getLessonDate() { return lessonDate; }    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }
    public LocalTime getStartTime() { return startTime; }    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }    public void setLocation(String location) { this.location = location; }
    public String getCancelReason() { return cancelReason; }    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getCompletedAt() { return completedAt; }    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public Long getCompletedBy() { return completedBy; }    public void setCompletedBy(Long completedBy) { this.completedBy = completedBy; }
    public String getSourceType() { return sourceType; }    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public BigDecimal getConsumeHours() { return consumeHours; }    public void setConsumeHours(BigDecimal consumeHours) { this.consumeHours = consumeHours; }
    public Integer getVersion() { return version; }    public void setVersion(Integer version) { this.version = version; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedBy() { return updatedBy; }    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
