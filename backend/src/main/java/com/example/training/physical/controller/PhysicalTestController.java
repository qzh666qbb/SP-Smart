package com.example.training.physical.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import com.example.training.physical.application.dto.PhysicalTestCreateRequest;
import com.example.training.physical.application.service.PhysicalTestApplicationService;
import com.example.training.physical.domain.model.PhysicalScoreDetail;
import com.example.training.physical.domain.model.PhysicalTestItem;
import com.example.training.physical.domain.model.PhysicalTestRecord;
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
    
    @PostMapping("/business/physical-tests")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> createTestRecord(
            @Valid @RequestBody PhysicalTestCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        PhysicalTestRecord response = physicalTestApplicationService.createTestRecord(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<PhysicalTestRecord>> listTestRecords(@AuthenticationPrincipal CurrentUser currentUser) {
        List<PhysicalTestRecord> response = physicalTestApplicationService.listTestRecords(currentUser.getProfileId());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> getRecordById(@PathVariable Long recordId) {
        PhysicalTestRecord response = physicalTestApplicationService.getRecordById(recordId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/business/physical-tests/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> updateRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody PhysicalTestCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        PhysicalTestRecord response = physicalTestApplicationService.updateRecord(recordId, request, currentUser);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/business/physical-tests/{recordId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PhysicalTestRecord> confirmRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        PhysicalTestRecord response = physicalTestApplicationService.confirmRecord(recordId, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<PhysicalTestItem>> getAllEnabledItems() {
        List<PhysicalTestItem> response = physicalTestApplicationService.getAllEnabledItems();
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/physical-tests/{recordId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<PhysicalScoreDetail>> getScoreDetails(@PathVariable Long recordId) {
        List<PhysicalScoreDetail> response = physicalTestApplicationService.getScoreDetails(recordId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/physical-tests/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<List<PhysicalTestRecord>> getRecordsByStudent(@PathVariable Long studentId) {
        List<PhysicalTestRecord> response = physicalTestApplicationService.getRecordsByStudent(studentId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/physical-tests/{studentId}/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<PhysicalTestRecord> getLatestRecord(@PathVariable Long studentId) {
        PhysicalTestRecord response = physicalTestApplicationService.getLatestRecord(studentId);
        return ApiResponse.success(response);
    }
}
