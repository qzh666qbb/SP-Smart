package com.example.training.schedule.controller;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.common.security.ParentPermissionService;
import com.example.training.common.security.TeacherPermissionService;
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
    private final TeacherPermissionService teacherPermissionService;
    
    private Long resolveCalendarTeacherId(CurrentUser currentUser) {
        if ("ADMIN".equals(currentUser.getRole())) return null;
        if (currentUser.getProfileId() == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前账号未关联教师资料，请联系管理员");
        }
        return currentUser.getProfileId();
    }

    @PostMapping("/business/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ScheduleSeries> createScheduleSeries(
            @Valid @RequestBody ScheduleCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        ScheduleSeries response = scheduleApplicationService.createScheduleSeries(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PageResult<ScheduleSeries>> listSchedules(
            @AuthenticationPrincipal CurrentUser currentUser,
            PageRequest pageRequest,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long teacherId = resolveCalendarTeacherId(currentUser);
        PageResult<ScheduleSeries> response = scheduleApplicationService.listSchedulesPage(
                teacherId, keyword, date, pageRequest.getPageNum(), pageRequest.getPageSize());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ScheduleSeries> getSchedule(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {
        ScheduleSeries response = scheduleApplicationService.getSchedule(id, currentUser);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/business/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        scheduleApplicationService.deleteSchedule(id);
        return ApiResponse.success();
    }

    @PutMapping("/business/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ScheduleSeries> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        ScheduleSeries response = scheduleApplicationService.updateSchedule(id, request, currentUser);
        return ApiResponse.success(response);
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
            @Valid @RequestBody AttendanceRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if (request.getLessonId() != null && !lessonId.equals(request.getLessonId())) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "路径课程ID与请求体不一致");
        }
        request.setLessonId(lessonId);
        if (!parentPermissionService.canAccessStudent(currentUser, request.getStudentId())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "非本人子女，无权限操作");
        }
        scheduleApplicationService.recordAttendance(request, currentUser);
        return ApiResponse.success();
    }
    
    @GetMapping("/business/lessons")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<Lesson>> getLessonsByTeacher(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CurrentUser currentUser) {
        Long teacherId = resolveCalendarTeacherId(currentUser);
        if (date != null) {
            if (startDate != null || endDate != null) {
                throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "date不能与日期范围同时使用");
            }
            return ApiResponse.success(scheduleApplicationService.getLessonsByTeacher(teacherId, date));
        }
        if ((startDate == null) != (endDate == null)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "开始日期和结束日期必须同时提供");
        }
        if (startDate == null) {
            // 旧版只加载一次且翻月不传范围；保留前一年至后一年的兼容窗口。
            startDate = LocalDate.now().minusYears(1).withDayOfYear(1);
            endDate = LocalDate.now().plusYears(1).withMonth(12).withDayOfMonth(31);
        } else if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "开始日期不能晚于结束日期");
        }
        List<Lesson> response = scheduleApplicationService.getLessonsByTeacher(teacherId, startDate, endDate);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Lesson> getLessonDetail(@PathVariable Long lessonId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(scheduleApplicationService.getLessonDetail(lessonId, currentUser));
    }

    @GetMapping("/business/lessons/{lessonId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<LessonStudent>> getLessonAttendance(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        List<LessonStudent> response = scheduleApplicationService.getLessonAttendance(lessonId, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/timetable")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<List<com.example.training.schedule.application.dto.PublicLessonResponse>> getPublicTimetable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate) || java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) > 6) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "请选择不超过一周的日期范围");
        }
        return ApiResponse.success(scheduleApplicationService.getLessonsByTeacher(null, startDate, endDate)
                .stream().map(com.example.training.schedule.application.dto.PublicLessonResponse::from).toList());
    }

    @GetMapping("/parent/schedules")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT', 'TEACHER')")
    public ApiResponse<List<Lesson>> getChildSchedules(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "开始日期不能晚于结束日期");
        }
        if ("PARENT".equals(currentUser.getRole())
                && !parentPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员排课信息");
        } else if ("TEACHER".equals(currentUser.getRole())
                && !teacherPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员排课信息");
        }
        List<Lesson> response = scheduleApplicationService.getChildSchedules(studentId, startDate, endDate, currentUser);
        return ApiResponse.success(response);
    }

    @PostMapping("/parent/lessons/{lessonId}/rating")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<Void> submitLessonRating(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if (body.get("studentId") == null || body.get("rating") == null) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "学员和评分不能为空");
        }
        Long studentId = Long.valueOf(body.get("studentId").toString());
        if (!parentPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        BigDecimal rating;
        try {
            rating = new BigDecimal(body.get("rating").toString());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "评分格式不正确");
        }
        if (rating.compareTo(BigDecimal.ONE) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "评分需1-5分");
        }
        String comment = body.get("comment") != null ? body.get("comment").toString() : null;
        scheduleApplicationService.submitRating(lessonId, studentId, rating, comment);
        return ApiResponse.success();
    }
}
