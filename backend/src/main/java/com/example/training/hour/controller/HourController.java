package com.example.training.hour.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import com.example.training.hour.application.dto.HourRechargeRequest;
import com.example.training.hour.application.dto.HourRefundRequest;
import com.example.training.hour.application.service.HourApplicationService;
import com.example.training.hour.domain.model.HourTransaction;
import com.example.training.hour.domain.model.StudentHourAccount;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HourController {
    
    private final HourApplicationService hourApplicationService;
    
    @PostMapping("/business/hours/recharge")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentHourAccount> recharge(
            @Valid @RequestBody HourRechargeRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentHourAccount response = hourApplicationService.recharge(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/business/hours/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentHourAccount> refund(
            @Valid @RequestBody HourRefundRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentHourAccount response = hourApplicationService.refund(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/hours/accounts/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentHourAccount> getAccount(@PathVariable Long studentId) {
        StudentHourAccount response = hourApplicationService.getAccount(studentId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/hours/transactions/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<HourTransaction>> getTransactions(@PathVariable Long studentId) {
        List<HourTransaction> response = hourApplicationService.getTransactions(studentId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/hours/accounts/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<StudentHourAccount> getParentAccount(@PathVariable Long studentId) {
        StudentHourAccount response = hourApplicationService.getAccount(studentId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/hours/transactions/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<List<HourTransaction>> getParentTransactions(@PathVariable Long studentId) {
        List<HourTransaction> response = hourApplicationService.getTransactions(studentId);
        return ApiResponse.success(response);
    }
}
