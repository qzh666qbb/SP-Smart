package com.example.training.hour.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.dto.HourPackageCreateRequest;
import com.example.training.hour.application.dto.HourPackageUpdateRequest;
import com.example.training.hour.application.service.HourPackageService;
import com.example.training.hour.domain.model.HourPackage;
import com.example.training.hour.domain.model.StudentPackageRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class HourPackageController {

    private final HourPackageService hourPackageService;

    @PostMapping("/hour-packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HourPackage> createPackage(
            @RequestBody HourPackageCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        HourPackage response = hourPackageService.createPackage(request, currentUser);
        return ApiResponse.success(response);
    }

    @PutMapping("/hour-packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HourPackage> updatePackage(
            @PathVariable Long id,
            @RequestBody HourPackageUpdateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        HourPackage response = hourPackageService.updatePackage(id, request, currentUser);
        return ApiResponse.success(response);
    }

    @PostMapping("/hour-packages/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HourPackage> disablePackage(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {
        HourPackage response = hourPackageService.disablePackage(id, currentUser);
        return ApiResponse.success(response);
    }

    @GetMapping("/hour-packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HourPackage>> listPackages() {
        List<HourPackage> response = hourPackageService.listAllPackages();
        return ApiResponse.success(response);
    }

    @PostMapping("/hour-packages/purchase")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StudentPackageRecord> purchasePackage(
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal CurrentUser currentUser) {
        Long studentId = body.get("studentId");
        Long packageId = body.get("packageId");
        StudentPackageRecord response = hourPackageService.purchasePackage(studentId, packageId, currentUser);
        return ApiResponse.success(response);
    }

    @GetMapping("/students/{studentId}/packages")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<StudentPackageRecord>> listStudentPackages(@PathVariable Long studentId) {
        List<StudentPackageRecord> response = hourPackageService.getStudentPackages(studentId);
        return ApiResponse.success(response);
    }
}
