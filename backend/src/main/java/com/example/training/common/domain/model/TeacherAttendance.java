package com.example.training.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
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
}
