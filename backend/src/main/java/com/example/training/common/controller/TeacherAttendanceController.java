package com.example.training.common.controller;

import com.example.training.common.domain.model.TeacherAttendance;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.infrastructure.repository.TeacherAttendanceRepository;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final TeacherAttendanceRepository teacherAttendanceRepository;

    @PostMapping("/api/admin/teacher-attendances")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TeacherAttendance> createAttendance(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {
        TeacherAttendance attendance = TeacherAttendance.builder()
                .teacherId(body.get("teacherId") != null ? Long.valueOf(body.get("teacherId").toString()) : null)
                .lessonId(body.get("lessonId") != null ? Long.valueOf(body.get("lessonId").toString()) : null)
                .attendanceDate(body.get("attendanceDate") != null ? LocalDate.parse(body.get("attendanceDate").toString()) : null)
                .attendanceStatus(body.get("attendanceStatus") != null ? body.get("attendanceStatus").toString() : "PRESENT")
                .checkInTime(body.get("checkInTime") != null ? LocalTime.parse(body.get("checkInTime").toString()) : null)
                .checkOutTime(body.get("checkOutTime") != null ? LocalTime.parse(body.get("checkOutTime").toString()) : null)
                .leaveReason(body.get("leaveReason") != null ? body.get("leaveReason").toString() : null)
                .createdBy(currentUser.getUserId())
                .build();
        TeacherAttendance saved = teacherAttendanceRepository.save(attendance);
        return ApiResponse.success(saved);
    }

    @PutMapping("/api/admin/teacher-attendances/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TeacherAttendance> updateAttendance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {
        TeacherAttendance attendance = teacherAttendanceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "考勤记录不存在"));

        if (body.get("attendanceStatus") != null) {
            attendance.setAttendanceStatus(body.get("attendanceStatus").toString());
        }
        if (body.get("checkInTime") != null) {
            attendance.setCheckInTime(LocalTime.parse(body.get("checkInTime").toString()));
        }
        if (body.get("checkOutTime") != null) {
            attendance.setCheckOutTime(LocalTime.parse(body.get("checkOutTime").toString()));
        }
        if (body.get("leaveReason") != null) {
            attendance.setLeaveReason(body.get("leaveReason").toString());
        }
        if (body.get("lessonId") != null) {
            attendance.setLessonId(Long.valueOf(body.get("lessonId").toString()));
        }

        TeacherAttendance saved = teacherAttendanceRepository.save(attendance);
        return ApiResponse.success(saved);
    }

    @GetMapping("/api/admin/teacher-attendances/teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TeacherAttendance>> listByTeacher(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendance> list = teacherAttendanceRepository.findByTeacherIdAndDateRange(teacherId, startDate, endDate);
        return ApiResponse.success(list);
    }

    @GetMapping("/api/admin/teacher-attendances")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TeacherAttendance>> listByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TeacherAttendance> list = teacherAttendanceRepository.findAllByDateRange(startDate, endDate);
        return ApiResponse.success(list);
    }

    @PostMapping("/api/business/teacher/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<TeacherAttendance> teacherSelfCheckIn(
            @RequestBody(required = false) Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {
        Long teacherId = currentUser.getProfileId();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Long lessonId = (body != null && body.get("lessonId") != null)
                ? Long.valueOf(body.get("lessonId").toString()) : null;

        TeacherAttendance attendance = TeacherAttendance.builder()
                .teacherId(teacherId)
                .lessonId(lessonId)
                .attendanceDate(today)
                .attendanceStatus("PRESENT")
                .checkInTime(now)
                .createdBy(currentUser.getUserId())
                .build();

        TeacherAttendance saved = teacherAttendanceRepository.save(attendance);
        return ApiResponse.success(saved);
    }
}
