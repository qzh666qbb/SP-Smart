package com.example.training.course.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import com.example.training.course.application.dto.ClassGroupCreateRequest;
import com.example.training.course.application.dto.CourseCreateRequest;
import com.example.training.course.application.dto.CourseResponse;
import com.example.training.course.application.service.CourseApplicationService;
import com.example.training.course.domain.model.ClassGroup;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseApplicationService courseApplicationService;
    
    @PostMapping("/business/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<CourseResponse> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        CourseResponse response = courseApplicationService.createCourse(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<CourseResponse>> listCourses() {
        List<CourseResponse> response = courseApplicationService.getAllEnabledCourses();
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable Long id) {
        CourseResponse response = courseApplicationService.getCourseById(id);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/business/courses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        CourseResponse response = courseApplicationService.getCourseById(id);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/business/courses/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> disableCourse(@PathVariable Long id) {
        courseApplicationService.disableCourse(id);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/class-groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassGroup> createClassGroup(
            @Valid @RequestBody ClassGroupCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        ClassGroup response = courseApplicationService.createClassGroup(request, currentUser);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/class-groups/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassGroup> getClassGroup(@PathVariable Long id) {
        ClassGroup response = courseApplicationService.getClassGroup(id);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/business/class-groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<ClassGroup>> listClassGroups() {
        List<ClassGroup> response = courseApplicationService.listClassGroups();
        return ApiResponse.success(response);
    }
    
    @PutMapping("/business/class-groups/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassGroup> updateClassGroup(
            @PathVariable Long id,
            @Valid @RequestBody ClassGroupCreateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        ClassGroup response = courseApplicationService.getClassGroup(id);
        return ApiResponse.success(response);
    }
    
    @PostMapping("/business/class-groups/{id}/add-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> addStudentsToClassGroup(
            @PathVariable Long id,
            @RequestBody List<Long> studentIds) {
        courseApplicationService.addStudentsToClassGroup(id, studentIds);
        return ApiResponse.success();
    }
    
    @PostMapping("/business/class-groups/{id}/remove-students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> removeStudentsFromClassGroup(
            @PathVariable Long id,
            @RequestBody List<Long> studentIds) {
        courseApplicationService.removeStudentsFromClassGroup(id, studentIds);
        return ApiResponse.success();
    }
}
