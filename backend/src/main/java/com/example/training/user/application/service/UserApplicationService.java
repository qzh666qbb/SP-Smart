package com.example.training.user.application.service;

import com.example.training.auth.domain.model.TeacherProfile;
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

import java.time.LocalDate;
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
        boolean teacher = "TEACHER".equals(request.getRole());
        String username = teacher ? requireTeacherPhone(request.getPhone()) : requireText(request.getUsername(), "用户名不能为空");
        String password = teacher && isBlank(request.getPassword()) ? defaultTempPassword
                : requireText(request.getPassword(), "密码不能为空");

        userRepository.findByUsername(username)
                .ifPresent(u -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "用户名已存在");
                });
        if (!isBlank(request.getPhone())) {
            userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
                throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
            });
        }
        if (teacher) {
            requireText(request.getTeacherName(), "教师姓名不能为空");
        }

        String status = request.getStatus() == null || request.getStatus().isEmpty() ? "ENABLED" : request.getStatus();
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .role(request.getRole())
                .phone(request.getPhone())
                .status(status)
                .passwordUpdatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        if (teacher) {
            userRepository.saveTeacherProfile(newTeacherProfile(savedUser, request.getTeacherName()));
        }
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

        String nextRole = request.getRole() == null ? user.getRole() : request.getRole();
        boolean teacher = "TEACHER".equals(nextRole);
        String nextUsername = teacher && request.getPhone() != null
                ? requireTeacherPhone(request.getPhone()) : request.getUsername();

        if (nextUsername != null && !nextUsername.equals(user.getUsername())) {
            userRepository.findByUsername(nextUsername).ifPresent(existing -> {
                if (!id.equals(existing.getId())) {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "用户名已存在");
                }
            });
            user.setUsername(nextUsername);
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getPhone() != null) {
            userRepository.findByPhone(request.getPhone()).ifPresent(existing -> {
                if (!id.equals(existing.getId())) {
                    throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
                }
            });
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPasswordUpdatedAt(LocalDateTime.now());
        } else if (teacher && isBlank(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(defaultTempPassword));
            user.setPasswordUpdatedAt(LocalDateTime.now());
        }

        User saved = userRepository.save(user);
        if (teacher) {
            TeacherProfile profile = userRepository.findTeacherProfileByUserId(saved.getId()).orElse(null);
            if (profile == null) {
                String name = requireText(request.getTeacherName(), "教师姓名不能为空");
                userRepository.saveTeacherProfile(newTeacherProfile(saved, name));
            } else if (!isBlank(request.getTeacherName())) {
                profile.setName(request.getTeacherName());
                userRepository.saveTeacherProfile(profile);
            }
        }
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

        user.setStatus("DISABLED");
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
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
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
        if (user.isTeacher()) {
            userRepository.findTeacherProfileByUserId(user.getId()).ifPresent(profile -> {
                response.setProfileId(profile.getId());
                response.setProfileName(profile.getName());
            });
        }
        return response;
    }

    private TeacherProfile newTeacherProfile(User user, String teacherName) {
        return TeacherProfile.builder()
                .userId(user.getId())
                .teacherNo("T" + String.format("%08d", user.getId()))
                .name(teacherName)
                .employmentStatus("ACTIVE")
                .joinedDate(LocalDate.now())
                .build();
    }

    private String requireTeacherPhone(String phone) {
        if (isBlank(phone)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "教师手机号不能为空");
        }
        return phone;
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, message);
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
