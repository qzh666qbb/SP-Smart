package com.example.training.user.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.user.application.dto.UserCreateRequest;
import com.example.training.user.application.dto.UserResponse;
import com.example.training.user.application.service.UserApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    private final UserApplicationService userApplicationService;
    
    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userApplicationService.createUser(request);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userApplicationService.getUserById(id);
        return ApiResponse.success(response);
    }
    
    @GetMapping
    public ApiResponse<PageResult<UserResponse>> listUsers(PageRequest pageRequest) {
        PageResult<UserResponse> response = userApplicationService.listUsers(pageRequest);
        return ApiResponse.success(response);
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return ApiResponse.success();
    }
}
