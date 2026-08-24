package com.example.training.user.controller;

import com.example.training.common.response.ApiResponse;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.user.application.dto.ResetPasswordRequest;
import com.example.training.user.application.dto.UserCreateRequest;
import com.example.training.user.application.dto.UserResponse;
import com.example.training.user.application.dto.UserUpdateRequest;
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
    public ApiResponse<PageResult<UserResponse>> listUsers(
            PageRequest pageRequest,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role) {
        PageResult<UserResponse> response = userApplicationService.listUsers(pageRequest, keyword, role);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userApplicationService.updateUser(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long id,
            @RequestBody(required = false) ResetPasswordRequest req) {
        if (req == null) {
            req = new ResetPasswordRequest();
        }
        req.setUserId(id);
        userApplicationService.resetPassword(id, req);
        return ApiResponse.success();
    }
}
