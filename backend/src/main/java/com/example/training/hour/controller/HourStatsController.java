package com.example.training.hour.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.hour.application.dto.HourConsumeStatsResponse;
import com.example.training.hour.application.dto.HourRankResponse;
import com.example.training.hour.application.service.HourStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/hour-stats")
@RequiredArgsConstructor
public class HourStatsController {

    private final HourStatsService hourStatsService;

    @GetMapping("/consume-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HourConsumeStatsResponse> getConsumeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String courseType) {
        HourConsumeStatsResponse response = hourStatsService.getConsumeStats(startDate, endDate, courseType);
        return ApiResponse.success(response);
    }

    @GetMapping("/teacher-rank")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HourRankResponse>> getTeacherConsumeRank(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int topN) {
        List<HourRankResponse> response = hourStatsService.getTeacherConsumeRank(startDate, endDate, topN);
        return ApiResponse.success(response);
    }

    @GetMapping("/student-rank")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HourRankResponse>> getStudentConsumeRank(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int topN) {
        List<HourRankResponse> response = hourStatsService.getStudentConsumeRank(startDate, endDate, topN);
        return ApiResponse.success(response);
    }

    @GetMapping("/attendance-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getAttendanceStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long teacherId) {
        return ApiResponse.success(hourStatsService.getAttendanceStats(startDate, endDate, teacherId));
    }
}
