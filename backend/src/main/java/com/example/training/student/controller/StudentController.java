package com.example.training.student.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import com.example.training.student.application.dto.BindRequest;
import com.example.training.student.application.dto.StudentCreateRequest;
import com.example.training.student.application.dto.StudentResponse;
import com.example.training.student.application.service.StudentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentApplicationService studentApplicationService;
    
    @PostMapping("/business/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentResponse> createStudent(
            @Valid @RequestBody StudentCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentResponse response = studentApplicationService.createStudent(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/business/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentResponse response = studentApplicationService.getStudentById(id);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<StudentResponse>> listStudents(@AuthenticationPrincipal CurrentUser currentUser) {
        List<StudentResponse> response = studentApplicationService.getStudentsByTeacher(currentUser.getProfileId());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentResponse> getStudentById(@PathVariable Long id) {
        StudentResponse response = studentApplicationService.getStudentById(id);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/business/students/{id}/bind-parent")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> bindParent(
            @PathVariable Long id,
            @Valid @RequestBody BindRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.bindStudent(request, currentUser);
        return ApiResponse.success();
    }
    
    @GetMapping("/parent/children")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<List<StudentResponse>> getChildren(@AuthenticationPrincipal CurrentUser currentUser) {
        List<StudentResponse> response = studentApplicationService.getStudentsByParent(currentUser.getUserId());
        return ApiResponse.success(response);
    }
    
    @GetMapping("/parent/children/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<StudentResponse> getChildById(@PathVariable Long studentId) {
        StudentResponse response = studentApplicationService.getStudentById(studentId);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/parent/bind")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<Void> bindStudent(
            @Valid @RequestBody BindRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.bindStudent(request, currentUser);
        return ApiResponse.success();
    }
    
    @PostMapping("/parent/bind/{relationId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<Void> confirmBind(
            @PathVariable Long relationId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.confirmBind(relationId, currentUser);
        return ApiResponse.success();
    }
    
    @PostMapping("/parent/unbind/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARENT')")
    public ApiResponse<Void> unbindStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.unbindStudent(studentId, currentUser);
        return ApiResponse.success();
    }
}
