package com.example.training.schedule.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Map;

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
    private String courseName;
    private String teacherName;
    private Map<Long, String> studentAttendance;
    private Long assistantTeacherId1;
    private Long assistantTeacherId2;
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

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public String getLessonNo() { return lessonNo; }    public void setLessonNo(String lessonNo) { this.lessonNo = lessonNo; }
    public Long getScheduleSeriesId() { return scheduleSeriesId; }    public void setScheduleSeriesId(Long scheduleSeriesId) { this.scheduleSeriesId = scheduleSeriesId; }
    public Long getCourseId() { return courseId; }    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getClassGroupId() { return classGroupId; }    public void setClassGroupId(Long classGroupId) { this.classGroupId = classGroupId; }
    public Long getTeacherId() { return teacherId; }    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getCourseName() { return courseName; }    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getTeacherName() { return teacherName; }    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public Map<Long, String> getStudentAttendance() { return studentAttendance; }    public void setStudentAttendance(Map<Long, String> studentAttendance) { this.studentAttendance = studentAttendance; }
    public Long getAssistantTeacherId1() { return assistantTeacherId1; }    public void setAssistantTeacherId1(Long assistantTeacherId1) { this.assistantTeacherId1 = assistantTeacherId1; }
    public Long getAssistantTeacherId2() { return assistantTeacherId2; }    public void setAssistantTeacherId2(Long assistantTeacherId2) { this.assistantTeacherId2 = assistantTeacherId2; }
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
