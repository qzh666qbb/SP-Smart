package com.example.training.user.application.service;

import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.user.application.dto.ResetPasswordRequest;
import com.example.training.user.application.dto.UserCreateRequest;
import com.example.training.user.application.dto.UserResponse;
import com.example.training.user.application.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserApplicationService {
    private static final Logger log = LoggerFactory.getLogger(UserApplicationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-temp-password:123456}")
    private String defaultTempPassword;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "用户名已存在");
                });

        String status = request.getStatus() == null || request.getStatus().isEmpty() ? "ENABLED" : request.getStatus();
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .status(status)
                .passwordUpdatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("创建用户: id={}, username={}, role={}", savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
        return convertToResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return convertToResponse(user);
    }

    public PageResult<UserResponse> listUsers(PageRequest pageRequest) {
        return listUsers(pageRequest, null, null);
    }

    public PageResult<UserResponse> listUsers(PageRequest pageRequest, String keyword, String role) {
        List<User> users = userRepository.findAll(keyword, role, pageRequest.getPageNum(), pageRequest.getPageSize());
        long total = userRepository.countAll(keyword, role);

        List<UserResponse> records = users.stream()
                .map(this::convertToResponse)
                .toList();

        return PageResult.of(records, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPasswordUpdatedAt(LocalDateTime.now());
        }

        User saved = userRepository.save(user);
        log.info("更新用户: id={}, username={}", id, saved.getUsername());
        return convertToResponse(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isAdmin()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "不能删除管理员");
        }

        log.info("删除用户: id={}, username={}", id, user.getUsername());
    }

    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newPwd = (req == null || req.getNewPassword() == null || req.getNewPassword().isBlank())
                ? defaultTempPassword
                : req.getNewPassword();

        user.setPasswordHash(passwordEncoder.encode(newPwd));
        user.setPasswordUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("重置用户密码: userId={}, username={}", userId, user.getUsername());
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
