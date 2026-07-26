package com.example.training.auth.domain.service;

import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        
        return result;
    }
    
    @Transactional
    public Map<String, Object> loginByWechat(String openid, String unionid) {
        User user = userRepository.findByWechatOpenid(openid)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username("wx_" + UUID.randomUUID().toString().substring(0, 8))
                            .role("PARENT")
                            .wechatOpenid(openid)
                            .wechatUnionid(unionid)
                            .status("ENABLED")
                            .build();
                    return userRepository.save(newUser);
                });
        
        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
        }
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        
        return result;
    }
    
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            Long userId = jwtService.extractUserId(refreshToken);
            String username = jwtService.extractUsername(refreshToken);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_TOKEN));
            
            String newAccessToken = jwtService.generateAccessToken(userId, username, user.getRole());
            String newRefreshToken = jwtService.generateRefreshToken(userId, username);
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            
            return result;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN, "无效的刷新令牌");
        }
    }
    
    public CurrentUser getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        CurrentUser.CurrentUserBuilder builder = CurrentUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .role(user.getRole())
                .wechatOpenid(user.getWechatOpenid())
                .wechatUnionid(user.getWechatUnionid());
        
        if (user.isTeacher()) {
            userRepository.findTeacherProfileByUserId(userId)
                    .ifPresent(profile -> {
                        builder.profileId(profile.getId());
                        builder.profileType("TEACHER");
                    });
        } else if (user.isParent()) {
            userRepository.findParentProfileByUserId(userId)
                    .ifPresent(profile -> {
                        builder.profileId(profile.getId());
                        builder.profileType("PARENT");
                    });
        }
        
        return builder.build();
    }
    
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
