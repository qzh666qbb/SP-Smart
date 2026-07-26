package com.example.training.report.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.report.application.service.ReportApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {
    
    private final ReportApplicationService reportApplicationService;
    
    @GetMapping("/attendance")
    public ApiResponse<Map<String, Object>> generateAttendanceReport(
            @RequestParam Long classGroupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = reportApplicationService.generateAttendanceReport(classGroupId, startDate, endDate);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/hours")
    public ApiResponse<Map<String, Object>> generateHourReport(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> response = reportApplicationService.generateHourReport(studentId, startDate, endDate);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/physical")
    public ApiResponse<Map<String, Object>> generatePhysicalReport(@RequestParam Long studentId) {
        Map<String, Object> response = reportApplicationService.generatePhysicalReport(studentId);
        return ApiResponse.success(response);
    }
}
