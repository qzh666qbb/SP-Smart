package com.example.training.schedule.controller;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import com.example.training.common.security.ParentPermissionService;
import com.example.training.schedule.application.dto.AttendanceRequest;
import com.example.training.schedule.application.dto.ScheduleCreateRequest;
import com.example.training.schedule.application.service.ScheduleApplicationService;
import com.example.training.schedule.application.service.ScheduleCompletionService;
import com.example.training.schedule.domain.model.Lesson;
import com.example.training.schedule.domain.model.LessonStudent;
import com.example.training.schedule.domain.model.ScheduleSeries;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleApplicationService scheduleApplicationService;
    private final ScheduleCompletionService scheduleCompletionService;
    private final ParentPermissionService parentPermissionService;
    
    @PostMapping("/business/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ScheduleSeries> createScheduleSeries(
            @Valid @RequestBody ScheduleCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        ScheduleSeries response = scheduleApplicationService.createScheduleSeries(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<ScheduleSeries>> listSchedules(@AuthenticationPrincipal CurrentUser currentUser) {
        List<ScheduleSeries> response = scheduleApplicationService.listSchedules(currentUser.getProfileId());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ScheduleSeries> getSchedule(@PathVariable Long id) {
        ScheduleSeries response = scheduleApplicationService.getSchedule(id);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/business/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        scheduleApplicationService.deleteSchedule(id);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/lessons/{lessonId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> startLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        scheduleApplicationService.startLesson(lessonId, currentUser);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/lessons/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> completeLessonWithConsume(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        scheduleCompletionService.completeLessonWithAutoConsume(id, currentUser);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/lessons/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> cancelLessonWithRefund(@PathVariable Long id, @RequestBody(required=false) Map<String, String> body, @AuthenticationPrincipal CurrentUser currentUser) {
        String reason = body != null ? body.getOrDefault("reason", "手动取消") : "手动取消";
        scheduleCompletionService.cancelLessonWithRefund(id, reason, currentUser);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> recordAttendance(
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        scheduleApplicationService.recordAttendance(request, currentUser);
        return ApiResponse.success();
    }
    
    @PostMapping("/parent/lessons/{lessonId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<Void> parentRecordAttendance(
            @PathVariable Long lessonId,
            @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if (!parentPermissionService.canAccessStudent(currentUser, request.getStudentId())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "非本人子女，无权限操作");
        }
        scheduleApplicationService.recordAttendance(request, currentUser);
        return ApiResponse.success();
    }
    
    @GetMapping("/business/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<Lesson>> getLessonsByTeacher(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CurrentUser currentUser) {
        List<Lesson> response = scheduleApplicationService.getLessonsByTeacher(currentUser.getProfileId(), date);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/lessons/{lessonId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<LessonStudent>> getLessonAttendance(@PathVariable Long lessonId) {
        List<LessonStudent> response = scheduleApplicationService.getLessonAttendance(lessonId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<List<Lesson>> getChildSchedules(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CurrentUser currentUser) {
        List<Lesson> response = scheduleApplicationService.getChildSchedules(studentId, startDate, endDate, currentUser);
        return ApiResponse.success(response);
    }

    @PostMapping("/parent/lessons/{lessonId}/rating")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<Void> submitLessonRating(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        if (!parentPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        BigDecimal rating = new BigDecimal(body.getOrDefault("rating", "0").toString());
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "评分需1-5分");
        }
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;
        scheduleApplicationService.submitRating(lessonId, studentId, rating, comment);
        return ApiResponse.success();
    }
}
