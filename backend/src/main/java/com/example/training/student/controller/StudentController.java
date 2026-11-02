package com.example.training.student.controller;

import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.common.security.CurrentUser;
import com.example.training.common.security.ParentPermissionService;
import com.example.training.common.security.TeacherPermissionService;
import com.example.training.student.application.dto.BindRequest;
import com.example.training.student.application.dto.ChildCreateRequest;
import com.example.training.student.application.dto.ParentBindConfirmRequest;
import com.example.training.student.application.dto.ParentBindMatchRequest;
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
    private final ParentPermissionService parentPermissionService;
    private final TeacherPermissionService teacherPermissionService;

    @PostMapping("/business/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentResponse> createStudent(
            @Valid @RequestBody StudentCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentResponse response = studentApplicationService.createStudent(request, currentUser);
        return ApiResponse.success(response);
    }

    @GetMapping("/business/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PageResult<StudentResponse>> listStudents(
            @AuthenticationPrincipal CurrentUser currentUser,
            PageRequest pageRequest,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Long teacherId = "ADMIN".equals(currentUser.getRole()) ? null : currentUser.getProfileId();
        PageResult<StudentResponse> response = studentApplicationService.getAllStudentsPage(
                keyword, teacherId, pageRequest.getPageNum(), pageRequest.getPageSize());
        return ApiResponse.success(response);
    }

    @GetMapping("/business/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'PARENT')")
    public ApiResponse<StudentResponse> getStudentById(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if ("PARENT".equals(currentUser.getRole())) {
            if (!parentPermissionService.canAccessStudent(currentUser, id)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员信息");
            }
        } else if ("TEACHER".equals(currentUser.getRole())) {
            if (!teacherPermissionService.canAccessStudent(currentUser, id)) {
                throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限查看该学员信息");
            }
        }
        StudentResponse response = studentApplicationService.getStudentById(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/business/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        if ("TEACHER".equals(currentUser.getRole())
                && !teacherPermissionService.canAccessStudent(currentUser, id)) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "无权限修改该学员信息");
        }
        StudentResponse response = studentApplicationService.updateStudent(id, request, currentUser);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/business/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteStudent(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.deleteStudent(id);
        return ApiResponse.success();
    }

    @PostMapping("/parent/students")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<StudentResponse> createChildByParent(
            @Valid @RequestBody ChildCreateRequest req,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentResponse resp = studentApplicationService.createChildByParent(currentUser, req);
        return ApiResponse.success(resp);
    }

    @GetMapping("/parent/students")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<StudentResponse>> getChildrenByParent(@AuthenticationPrincipal CurrentUser currentUser) {
        List<StudentResponse> resp = studentApplicationService.getStudentsByParent(currentUser.getProfileId());
        return ApiResponse.success(resp);
    }

    @GetMapping("/parent/children")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<List<StudentResponse>> getChildren(@AuthenticationPrincipal CurrentUser currentUser) {
        List<StudentResponse> response = studentApplicationService.getStudentsByParent(currentUser.getProfileId());
        return ApiResponse.success(response);
    }

    @GetMapping("/parent/children/{studentId}")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<StudentResponse> getChildById(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentResponse response = studentApplicationService.getChildById(currentUser.getProfileId(), studentId);
        return ApiResponse.success(response);
    }

    @PostMapping("/parent/bind-match")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<StudentResponse> matchStudentForBind(
            @Valid @RequestBody ParentBindMatchRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        StudentResponse response = studentApplicationService.matchStudentForBind(currentUser, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/parent/bind")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<Void> bind(
            @Valid @RequestBody ParentBindConfirmRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.parentConfirmBind(request.getStudentId(), request.getRelationType(), currentUser);
        return ApiResponse.success();
    }

    @PostMapping("/parent/bind-student")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<Void> bindStudentByNo(
            @Valid @RequestBody ParentBindConfirmRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        return bind(request, currentUser);
    }

    @PostMapping("/parent/bind/{relationId}/confirm")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<Void> confirmBind(
            @PathVariable Long relationId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.confirmBind(relationId, currentUser);
        return ApiResponse.success();
    }

    @PostMapping("/parent/unbind/{studentId}")
    @PreAuthorize("hasRole('PARENT')")
    public ApiResponse<Void> unbindStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal CurrentUser currentUser) {
        studentApplicationService.unbindStudent(studentId, currentUser);
        return ApiResponse.success();
    }
}
