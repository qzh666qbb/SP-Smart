package com.example.training.user.application.service;

import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.PageRequest;
import com.example.training.common.response.PageResult;
import com.example.training.user.application.dto.UserCreateRequest;
import com.example.training.user.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "用户名已存在");
                });
        
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .status("ENABLED")
                .build();
        
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return convertToResponse(user);
    }
    
    public PageResult<UserResponse> listUsers(PageRequest pageRequest) {
        com.baomidou.mybatisplus.core.metadata.IPage<User> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        pageRequest.getPageNum(), pageRequest.getPageSize());
        
        return PageResult.of(List.of(), 0, pageRequest.getPageNum(), pageRequest.getPageSize());
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
