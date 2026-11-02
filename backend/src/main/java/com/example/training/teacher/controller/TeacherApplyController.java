package com.example.training.teacher.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.teacher.application.dto.TeacherApplyReviewRequest;
import com.example.training.teacher.application.dto.TeacherApplySubmitRequest;
import com.example.training.teacher.application.dto.TeacherPreRegisterRequest;
import com.example.training.teacher.application.service.TeacherApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeacherApplyController {

    private final TeacherApplicationService teacherApplicationService;

    @PostMapping("/public/teacher/apply/submit")
    public ApiResponse<Long> submitTeacherApply(@Valid @RequestBody TeacherApplySubmitRequest req) {
        Long applyId = teacherApplicationService.submitApply(req);
        return ApiResponse.success(applyId);
    }

    @GetMapping("/admin/teacher/applies")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<Map<String, Object>>> listApplies(
            PageRequest pageRequest,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {
        PageResult<Map<String, Object>> resp = teacherApplicationService.listApplies(
                keyword, status, pageRequest.getPageNum(), pageRequest.getPageSize());
        return ApiResponse.success(resp);
    }

    @PostMapping("/admin/teacher/applies/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> reviewApply(
            @PathVariable Long id,
            @Valid @RequestBody TeacherApplyReviewRequest req,
            @AuthenticationPrincipal CurrentUser currentUser) {
        req.setApplyId(id);
        Map<String, Object> result = teacherApplicationService.reviewApply(id, req, currentUser);
        return ApiResponse.success(result);
    }

    @PostMapping("/admin/teacher/pre-register")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> preRegister(
            @Valid @RequestBody TeacherPreRegisterRequest req,
            @AuthenticationPrincipal CurrentUser currentUser) {
        Long id = teacherApplicationService.preRegister(req, currentUser);
        return ApiResponse.success(id);
    }

    @GetMapping("/admin/teacher/pre-register")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<Map<String, Object>>> listPreRegisters(
            PageRequest pageRequest,
            @RequestParam(value = "keyword", required = false) String keyword) {
        PageResult<Map<String, Object>> resp = teacherApplicationService.listPreRegisters(
                keyword, pageRequest.getPageNum(), pageRequest.getPageSize());
        return ApiResponse.success(resp);
    }

    @PutMapping("/admin/teacher/pre-register/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updatePreRegisterStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        teacherApplicationService.updatePreRegisterStatus(id, body == null ? null : body.get("status"));
        return ApiResponse.success();
    }
}
