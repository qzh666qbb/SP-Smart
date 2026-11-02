package com.example.training.auth.controller;

import com.example.training.auth.application.dto.LoginRequest;
import com.example.training.auth.application.dto.ParentRegisterRequest;
import com.example.training.auth.application.dto.ParentProfileCompleteRequest;
import com.example.training.auth.application.dto.RefreshTokenRequest;
import com.example.training.auth.application.dto.WechatBindRequest;
import com.example.training.auth.application.dto.WechatLoginRequest;
import com.example.training.auth.application.dto.WechatProfileUpdateRequest;
import com.example.training.auth.domain.service.AuthenticationService;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = authenticationService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(result);
    }

    @PostMapping("/register-parent")
    public ApiResponse<Map<String, Object>> registerParent(@Valid @RequestBody ParentRegisterRequest req) {
        Map<String, Object> result = authenticationService.registerParent(req);
        return ApiResponse.success(result);
    }

    @PostMapping("/login/wechat")
    public ApiResponse<Map<String, Object>> loginByWechat(@Valid @RequestBody WechatLoginRequest request) {
        Map<String, Object> result = authenticationService.loginByWechat(
                request.getCode(), request.getRole(), request.getNickname(), request.getAvatarUrl());
        return ApiResponse.success(result);
    }

    @PostMapping("/wechat/bind")
    public ApiResponse<Map<String, Object>> bindWechat(
            @Valid @RequestBody WechatBindRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        requireLogin(currentUser);
        return ApiResponse.success(authenticationService.bindWechat(currentUser.getUserId(),
                request.getCode(), request.getNickname(), request.getAvatarUrl()));
    }

    @PostMapping("/parent/profile/complete")
    public ApiResponse<Map<String, Object>> completeParentProfile(
            @Valid @RequestBody ParentProfileCompleteRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        requireLogin(currentUser);
        return ApiResponse.success(authenticationService.completeParentProfile(currentUser.getUserId(),
                request.getName(), request.getPhone(), request.getRelation()));
    }

    @PostMapping("/wechat/profile")
    public ApiResponse<Map<String, Object>> updateWechatProfile(
            @Valid @RequestBody WechatProfileUpdateRequest request,
            @AuthenticationPrincipal CurrentUser currentUser) {
        requireLogin(currentUser);
        return ApiResponse.success(authenticationService.updateWechatProfile(
                currentUser.getUserId(), request.getNickname(), request.getAvatarUrl()));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, Object> result = authenticationService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(result);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CurrentUser currentUser) {
        requireLogin(currentUser);
        authenticationService.logout(currentUser.getUserId());
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal CurrentUser currentUser) {
        requireLogin(currentUser);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", currentUser.getUserId());
        result.put("username", currentUser.getUsername());
        result.put("phone", currentUser.getPhone());
        result.put("role", currentUser.getRole());
        result.put("profileId", currentUser.getProfileId());
        result.put("profileType", currentUser.getProfileType());
        result.putAll(authenticationService.getUserProfileFields(currentUser.getUserId()));
        return ApiResponse.success(result);
    }

    private void requireLogin(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.AUTH_UNAUTHORIZED, "请先登录");
        }
    }
}
