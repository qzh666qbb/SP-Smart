package com.example.training.common.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("teacher_attendance")
public class TeacherAttendanceEntity {
    @TableId(type = IdType.AUTO)
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
