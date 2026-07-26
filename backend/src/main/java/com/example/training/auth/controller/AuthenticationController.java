package com.example.training.auth.controller;

import com.example.training.auth.application.dto.LoginRequest;
import com.example.training.auth.application.dto.RefreshTokenRequest;
import com.example.training.auth.application.dto.WechatLoginRequest;
import com.example.training.auth.domain.service.AuthenticationService;
import com.example.training.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    @PostMapping("/login/wechat")
    public ApiResponse<Map<String, Object>> loginByWechat(@Valid @RequestBody WechatLoginRequest request) {
        Map<String, Object> result = authenticationService.loginByWechat(request.getOpenid(), request.getUnionid());
        return ApiResponse.success(result);
    }
    
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, Object> result = authenticationService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(result);
    }
}
