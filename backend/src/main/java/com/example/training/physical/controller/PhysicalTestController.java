package com.example.training.physical.controller;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.common.security.ParentPermissionService;
import com.example.training.common.security.TeacherPermissionService;
import com.example.training.physical.application.dto.PhysicalTestCreateRequest;
import com.example.training.physical.application.dto.PhysicalTestBatchCreateRequest;
import com.example.training.physical.application.service.PhysicalTestApplicationService;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
import com.example.training.physical.domain.model.PhysicalTestBatch;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhysicalTestController {
    
    private final PhysicalTestApplicationService physicalTestApplicationService;
    private final ParentPermissionService parentPermissionService;
    private final TeacherPermissionService teacherPermissionService;

    @PostMapping("/business/physical-tests")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> createTestRecord(
            @Valid @RequestBody PhysicalTestCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        assertTeacherCanAccessStudent(currentUser, request.getStudentId());
        PhysicalTestRecord response = physicalTestApplicationService.createTestRecord(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PageResult<PhysicalTestRecord>> listTestRecords(
            @AuthenticationPrincipal CurrentUser currentUser,
            PageRequest pageRequest,
            @RequestParam(value = "keyword", required = false) String keyword) {
        PageResult<PhysicalTestRecord> response = "TEACHER".equals(currentUser.getRole())
                ? physicalTestApplicationService.listTestRecordsPageForTeacher(
                        currentUser.getProfileId(), keyword, pageRequest.getPageNum(), pageRequest.getPageSize())
                : physicalTestApplicationService.listTestRecordsPage(
                        null, keyword, pageRequest.getPageNum(), pageRequest.getPageSize());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> getRecordById(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        assertCanAccessRecord(currentUser, recordId);
        PhysicalTestRecord response = physicalTestApplicationService.getRecordById(recordId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/business/physical-tests/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> updateRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody PhysicalTestCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        assertCanAccessRecord(currentUser, recordId);
        assertTeacherCanAccessStudent(currentUser, request.getStudentId());
        PhysicalTestRecord response = physicalTestApplicationService.updateRecord(recordId, request, currentUser);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/business/physical-tests/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRecord(@PathVariable Long recordId) {
        physicalTestApplicationService.deleteTestRecord(recordId);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/physical-tests/{recordId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> confirmRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        assertCanAccessRecord(currentUser, recordId);
        PhysicalTestRecord response = physicalTestApplicationService.confirmRecord(recordId, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<PhysicalTestItem>> getAllEnabledItems() {
        List<PhysicalTestItem> response = physicalTestApplicationService.getAllEnabledItems();
        return ApiResponse.success(response);
    }

    @GetMapping("/business/physical-tests/batches")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<PhysicalTestBatch>> getAvailableBatches() {
        return ApiResponse.success(physicalTestApplicationService.getAvailableBatches());
    }

    @PostMapping("/business/physical-tests/batches")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PhysicalTestBatch> createBatch(
            @Valid @RequestBody PhysicalTestBatchCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(physicalTestApplicationService.createBatch(request, currentUser));
    }
    
    @GetMapping("/business/physical-tests/{recordId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<PhysicalScoreDetail>> getScoreDetails(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        assertCanAccessRecord(currentUser, recordId);
        List<PhysicalScoreDetail> response = physicalTestApplicationService.getScoreDetails(recordId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/physical-tests/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT', 'TEACHER')")
    public ApiResponse<List<PhysicalTestRecord>> getRecordsByStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if ("PARENT".equals(currentUser.getRole())
                && !parentPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员体测信息");
        } else if ("TEACHER".equals(currentUser.getRole())
                && !teacherPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员体测信息");
        }
        List<PhysicalTestRecord> response = physicalTestApplicationService.getRecordsByStudent(studentId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/physical-tests/{studentId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> getLatestRecord(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if ("PARENT".equals(currentUser.getRole())
                && !parentPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员体测信息");
        } else if ("TEACHER".equals(currentUser.getRole())
                && !teacherPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员体测信息");
        }
        PhysicalTestRecord response = physicalTestApplicationService.getLatestRecord(studentId);
        return ApiResponse.success(response);
    }

    private void assertCanAccessRecord(CurrentUser currentUser, Long recordId) {
        if (!"TEACHER".equals(currentUser.getRole())) return;
        PhysicalTestRecord record = physicalTestApplicationService.getRecordById(recordId);
        if (!currentUser.getUserId().equals(record.getTesterId())
                && !teacherPermissionService.canAccessStudent(currentUser, record.getStudentId())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限访问该体测记录");
        }
    }

    private void assertTeacherCanAccessStudent(CurrentUser currentUser, Long studentId) {
        if ("TEACHER".equals(currentUser.getRole())
                && !teacherPermissionService.canAccessStudent(currentUser, studentId)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限操作该学员体测信息");
        }
    }
}
