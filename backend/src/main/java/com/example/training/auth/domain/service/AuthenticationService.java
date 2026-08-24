package com.example.training.auth.domain.service;

import com.example.training.auth.application.dto.WechatSessionResponse;
import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.teacher.domain.model.TeacherApply;
import com.example.training.teacher.domain.repository.TeacherRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final TeacherRepository teacherRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.wechat.appid:}")
    private String wechatAppid;

    @Value("${app.wechat.secret:}")
    private String wechatSecret;

    @Value("${app.wechat.mock-openid-when-disabled:true}")
    private boolean mockOpenidWhenDisabled;

    @Transactional
    public Map<String, Object> registerParent(com.example.training.auth.application.dto.ParentRegisterRequest req) {
        userRepository.findByPhone(req.getPhone())
                .ifPresent(u -> {
                    throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
                });

        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new BusinessException(ErrorCode.USER_WEAK_PASSWORD);
        }

        User user = User.builder()
                .username(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("PARENT")
                .phone(req.getPhone())
                .status("ENABLED")
                .passwordUpdatedAt(LocalDateTime.now())
                .build();

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("phone") || msg.contains("username")) {
                throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
            }
            throw e;
        }

        ParentProfile profile = ParentProfile.builder()
                .userId(savedUser.getId())
                .name(req.getRealName())
                .phone(req.getPhone())
                .relationRemark(req.getDefaultRelation())
                .build();
        userRepository.saveParentProfile(profile);

        return login(req.getPhone(), req.getPassword());
    }

    @Transactional
    public WechatSessionResponse exchangeWechatCode(String code, String encryptedData, String iv, CurrentUser currentUser) {
        String openid;
        String unionid = null;
        String decryptedPhone = null;

        boolean wechatDisabled = wechatAppid == null || wechatAppid.isBlank()
                || wechatSecret == null || wechatSecret.isBlank();

        if (wechatDisabled && mockOpenidWhenDisabled) {
            if (code != null && code.startsWith("mock_")) {
                String sub = code.substring(5);
                if (sub.length() >= 8) {
                    openid = "ox_" + sub.substring(0, 8);
                } else {
                    String pad = String.format("%-8s", sub).replace(' ', '0');
                    openid = "ox_" + pad;
                }
                if (code.startsWith("mock_phone_")) {
                    String phonePart = code.substring("mock_phone_".length());
                    if (phonePart.matches("^1[3-9]\\d{9}.*$")) {
                        decryptedPhone = phonePart.substring(0, 11);
                    }
                }
            } else {
                throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
            }
        } else {
            try {
                String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wechatAppid
                        + "&secret=" + wechatSecret
                        + "&js_code=" + code
                        + "&grant_type=authorization_code";
                String resp = restTemplate.getForObject(url, String.class);
                JsonNode node = objectMapper.readTree(resp);
                if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                    throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
                }
                openid = node.has("openid") ? node.get("openid").asText() : null;
                unionid = node.has("unionid") && !node.get("unionid").isNull() ? node.get("unionid").asText() : null;
                if (openid == null || openid.isBlank()) {
                    throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
                }
            } catch (BusinessException be) {
                throw be;
            } catch (Exception e) {
                log.warn("微信code2session失败: {}", e.getMessage());
                throw new BusinessException(ErrorCode.WECHAT_CODE_EXCHANGE_FAILED);
            }
        }

        if (encryptedData != null && !encryptedData.isBlank() && iv != null && !iv.isBlank()) {
        }

        User boundUser = userRepository.findByWechatOpenid(openid).orElse(null);
        if (boundUser != null) {
            WechatSessionResponse.WechatSessionResponseBuilder b = WechatSessionResponse.builder()
                    .openid(openid)
                    .unionid(unionid)
                    .phone(decryptedPhone != null ? decryptedPhone : boundUser.getPhone())
                    .needReview(false);
            return b.build();
        }

        String lookupPhone = (decryptedPhone != null && !decryptedPhone.isBlank()) ? decryptedPhone : null;
        TeacherApply apply = teacherRepository.findApplyByPhoneOrOpenid(lookupPhone, openid).orElse(null);

        if (apply != null) {
            switch (apply.getStatus()) {
                case "PENDING":
                    return WechatSessionResponse.builder()
                            .openid(openid)
                            .unionid(unionid)
                            .phone(decryptedPhone != null ? decryptedPhone : apply.getPhone())
                            .needReview(true)
                            .pendingRole("TEACHER")
                            .pendingApplyId(apply.getId())
                            .build();
                case "APPROVED":
                    String finalOpenid = openid;
                    String finalUnionid = unionid;
                    userRepository.findByPhone(apply.getPhone()).ifPresent(u -> {
                        if (u.getWechatOpenid() == null || u.getWechatOpenid().isBlank()) {
                            u.setWechatOpenid(finalOpenid);
                            u.setWechatUnionid(finalUnionid);
                            userRepository.save(u);
                        }
                    });
                    return WechatSessionResponse.builder()
                            .openid(openid)
                            .unionid(unionid)
                            .phone(apply.getPhone())
                            .needReview(false)
                            .build();
                case "REJECTED":
                    throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "教师申请已被拒绝，请联系管理员");
                default:
                    break;
            }
        }

        if (lookupPhone != null) {
            if (teacherRepository.findPreRegisterByPhone(lookupPhone).isPresent()) {
                TeacherApply newApply = TeacherApply.builder()
                        .phone(lookupPhone)
                        .realName("")
                        .wechatOpenid(openid)
                        .status("PENDING")
                        .build();
                teacherRepository.saveApply(newApply);
                return WechatSessionResponse.builder()
                        .openid(openid)
                        .unionid(unionid)
                        .phone(lookupPhone)
                        .needReview(true)
                        .pendingRole("TEACHER")
                        .pendingApplyId(newApply.getId())
                        .build();
            }
        }

        return WechatSessionResponse.builder()
                .openid(openid)
                .unionid(unionid)
                .phone(decryptedPhone)
                .needReview(false)
                .build();
    }

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

        try {
            userRepository.updateLastLoginAt(user.getId());
        } catch (Exception e) {
            log.warn("Failed to update lastLoginAt for user {}: {}", user.getId(), e.getMessage());
        }

        CurrentUser cu = getCurrentUser(user.getId());
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole(),
                cu.getPhone(), cu.getProfileId(), cu.getProfileType());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("profileId", cu.getProfileId());
        result.put("profileType", cu.getProfileType());

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

        try {
            userRepository.updateLastLoginAt(user.getId());
        } catch (Exception e) {
            log.warn("Failed to update lastLoginAt for user {}: {}", user.getId(), e.getMessage());
        }

        CurrentUser cu = getCurrentUser(user.getId());
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getUsername(), user.getRole(),
                cu.getPhone(), cu.getProfileId(), cu.getProfileType());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("profileId", cu.getProfileId());
        result.put("profileType", cu.getProfileType());

        return result;
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            Long userId = jwtService.extractUserId(refreshToken);
            String username = jwtService.extractUsername(refreshToken);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_TOKEN));

            CurrentUser cu = getCurrentUser(userId);
            String newAccessToken = jwtService.generateAccessToken(
                    userId, username, user.getRole(),
                    cu.getPhone(), cu.getProfileId(), cu.getProfileType());
            String newRefreshToken = jwtService.generateRefreshToken(userId, username);

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
            result.put("profileId", cu.getProfileId());
            result.put("profileType", cu.getProfileType());

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
                    .ifPresentOrElse(profile -> {
                        builder.profileId(profile.getId());
                        builder.profileType("TEACHER");
                    }, () -> {
                        builder.profileId(userId);
                        builder.profileType("TEACHER");
                    });
        } else if (user.isParent()) {
            userRepository.findParentProfileByUserId(userId)
                    .ifPresentOrElse(profile -> {
                        builder.profileId(profile.getId());
                        builder.profileType("PARENT");
                    }, () -> {
                        builder.profileId(userId);
                        builder.profileType("PARENT");
                    });
        }

        return builder.build();
    }

    @Transactional
    public void logout(Long userId) {
        try {
            userRepository.updateLastLoginAt(userId);
        } catch (Exception e) {
            log.warn("Failed to update lastLoginAt for user {}: {}", userId, e.getMessage());
        }
    }
}
