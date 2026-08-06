package com.example.training.common.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
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
}
