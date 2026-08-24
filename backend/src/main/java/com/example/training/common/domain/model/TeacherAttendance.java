package com.example.training.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAttendance {

    private Long id;
    private Long teacherId;
    private Long lessonId;
    private LocalDate attendanceDate;
    private String attendanceStatus;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String leaveReason;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isPresent() {
        return "PRESENT".equals(attendanceStatus);
    }

    public boolean isLeave() {
        return "LEAVE".equals(attendanceStatus);
    }

    public boolean isAbsent() {
        return "ABSENT".equals(attendanceStatus);
    }

    public Long getId() { return id; }    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getLessonId() { return lessonId; }    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getAttendanceStatus() { return attendanceStatus; }    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public LocalTime getCheckInTime() { return checkInTime; }    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getLeaveReason() { return leaveReason; }    public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }
    public Long getCreatedBy() { return createdBy; }    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
