package com.example.training.auth.domain.service;

import com.example.training.auth.application.dto.ParentRegisterRequest;
import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.security.CurrentUser;
import com.example.training.teacher.domain.model.TeacherApply;
import com.example.training.teacher.domain.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final TeacherRepository teacherRepository;
    private final WechatGateway wechatGateway;

    @Transactional
    public Map<String, Object> registerParent(ParentRegisterRequest req) {
        userRepository.findByPhone(req.getPhone()).ifPresent(u -> {
            throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        });
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new BusinessException(ErrorCode.USER_WEAK_PASSWORD);
        }
        User user = User.builder().username(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword())).role("PARENT")
                .phone(req.getPhone()).status("ENABLED").passwordUpdatedAt(LocalDateTime.now()).build();
        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }
        userRepository.saveParentProfile(ParentProfile.builder().userId(savedUser.getId())
                .name(req.getRealName()).phone(req.getPhone()).relationRemark(req.getDefaultRelation()).build());
        return issueTokens(savedUser);
    }

    @Transactional
    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByPhone(username))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));
        ensureEnabled(user);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
        updateLastLogin(user);
        return issueTokens(user);
    }

    /** 微信 code 只能在服务端兑换；客户端不能提交 openid 换取 JWT。 */
    @Transactional
    public Map<String, Object> loginByWechat(String code, String requestedRole,
                                              String nickname, String avatarUrl) {
        validateWechatRole(requestedRole);
        WechatGateway.WechatIdentity identity = wechatGateway.exchangeCode(code);
        User user = findByWechatIdentity(identity);
        if (user == null) {
            user = "TEACHER".equals(requestedRole)
                    ? resolveApprovedTeacher(identity)
                    : createWechatParent(identity, nickname, avatarUrl);
        } else {
            ensureExistingTeacherHasProfile(user);
            if (applyWechatFields(user, identity, nickname, avatarUrl)) user = userRepository.save(user);
        }
        ensureEnabled(user);
        updateLastLogin(user);
        return issueTokens(user);
    }

    @Transactional
    public Map<String, Object> bindWechat(Long userId, String code, String nickname, String avatarUrl) {
        User current = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ensureEnabled(current);
        WechatGateway.WechatIdentity identity = wechatGateway.exchangeCode(code);
        User occupied = findByWechatIdentity(identity);
        if (occupied != null && !occupied.getId().equals(current.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "该微信已绑定其他账号");
        }
        if (current.getWechatOpenid() != null && !current.getWechatOpenid().isBlank()
                && !current.getWechatOpenid().equals(identity.openid())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "当前账号已绑定其他微信，请先联系管理员解绑");
        }
        applyWechatFields(current, identity, nickname, avatarUrl);
        return issueTokens(userRepository.save(current));
    }

    @Transactional
    public Map<String, Object> completeParentProfile(Long userId, String name, String phone, String relation) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.isParent()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "仅家长账号可以完善家长资料");
        }
        User phoneOwner = userRepository.findByPhone(phone).orElse(null);
        if (phoneOwner != null && !phoneOwner.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXISTS,
                    "该手机号已关联其他账号，请使用账号密码登录后绑定微信");
        }
        ParentProfile profile = userRepository.findParentProfileByUserId(userId)
                .orElseGet(() -> ParentProfile.builder().userId(userId).build());
        profile.setName(name.trim());
        profile.setPhone(phone.trim());
        profile.setRelationRemark(relation);
        userRepository.saveParentProfile(profile);
        user.setPhone(phone.trim());
        if (trimToNull(user.getNickname()) == null) user.setNickname(name.trim());
        return issueTokens(userRepository.save(user));
    }

    @Transactional
    public Map<String, Object> updateWechatProfile(Long userId, String nickname, String avatarUrl) {
        String normalizedNickname = trimToNull(nickname);
        String normalizedAvatarUrl = trimToNull(avatarUrl);
        if (normalizedNickname == null && normalizedAvatarUrl == null) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "微信头像和昵称不能为空");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (normalizedNickname != null) user.setNickname(normalizedNickname);
        if (normalizedAvatarUrl != null) user.setAvatarUrl(normalizedAvatarUrl);
        user = userRepository.save(user);
        Map<String, Object> result = new HashMap<>();
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatarUrl());
        result.put("wechatBound", isWechatBound(user));
        return result;
    }

    public Map<String, Object> getUserProfileFields(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        CurrentUser currentUser = getCurrentUser(userId);
        Map<String, Object> fields = new HashMap<>();
        fields.put("nickname", user.getNickname());
        fields.put("avatar", user.getAvatarUrl());
        fields.put("wechatBound", isWechatBound(user));
        fields.put("profileComplete", currentUser.getProfileId() != null);
        return fields;
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            if (!jwtService.isRefreshToken(refreshToken) || jwtService.isTokenExpired(refreshToken)) {
                throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
            }
            User user = userRepository.findById(jwtService.extractUserId(refreshToken))
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_TOKEN));
            ensureEnabled(user);
            if (jwtService.extractTokenVersion(refreshToken) != tokenVersion(user)) {
                throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN, "刷新令牌已失效，请重新登录");
            }
            return issueTokens(user);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN, "无效的刷新令牌");
        }
    }

    public CurrentUser getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        CurrentUser.CurrentUserBuilder builder = CurrentUser.builder().userId(user.getId())
                .username(user.getUsername()).phone(user.getPhone()).role(user.getRole())
                .wechatOpenid(user.getWechatOpenid()).wechatUnionid(user.getWechatUnionid());
        if (user.isTeacher()) {
            userRepository.findTeacherProfileByUserId(userId).ifPresent(profile -> builder
                    .profileId(profile.getId()).profileType("TEACHER"));
        } else if (user.isParent()) {
            userRepository.findParentProfileByUserId(userId).ifPresent(profile -> builder
                    .profileId(profile.getId()).profileType("PARENT"));
        }
        return builder.build();
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setTokenVersion(tokenVersion(user) + 1);
        userRepository.save(user);
    }

    private User resolveApprovedTeacher(WechatGateway.WechatIdentity identity) {
        TeacherApply apply = teacherRepository.findApplyByOpenid(identity.openid()).orElse(null);
        if (apply == null) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "教师账号尚未申请或未关联本微信");
        }
        if ("PENDING".equals(apply.getStatus())) throw new BusinessException(ErrorCode.TEACHER_APPLY_PENDING);
        if (!"APPROVED".equals(apply.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "教师申请未通过，请联系管理员");
        }
        User user = userRepository.findByPhone(apply.getPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "审核通过但教师账号尚未创建"));
        if (!user.isTeacher() || userRepository.findTeacherProfileByUserId(user.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "教师资料尚未完成，请联系管理员");
        }
        applyWechatFields(user, identity, null, null);
        return userRepository.save(user);
    }

    private User createWechatParent(WechatGateway.WechatIdentity identity, String nickname, String avatarUrl) {
        User newUser = User.builder().username("wx_" + UUID.randomUUID().toString().substring(0, 8))
                .role("PARENT").nickname(trimToNull(nickname)).avatarUrl(trimToNull(avatarUrl))
                .wechatOpenid(identity.openid()).wechatUnionid(identity.unionid()).status("ENABLED").build();
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            User existing = findByWechatIdentity(identity);
            if (existing != null) return existing;
            throw e;
        }
    }

    private User findByWechatIdentity(WechatGateway.WechatIdentity identity) {
        User byOpenid = userRepository.findByWechatOpenid(identity.openid()).orElse(null);
        User byUnionid = userRepository.findByWechatUnionid(identity.unionid()).orElse(null);
        if (byOpenid != null && byUnionid != null && !byOpenid.getId().equals(byUnionid.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "微信身份关联冲突，请联系管理员");
        }
        return byOpenid != null ? byOpenid : byUnionid;
    }

    private boolean applyWechatFields(User user, WechatGateway.WechatIdentity identity,
                                      String nickname, String avatarUrl) {
        boolean changed = false;
        if (!identity.openid().equals(user.getWechatOpenid())) {
            user.setWechatOpenid(identity.openid()); changed = true;
        }
        if (trimToNull(identity.unionid()) != null && !identity.unionid().equals(user.getWechatUnionid())) {
            user.setWechatUnionid(identity.unionid()); changed = true;
        }
        String normalizedNickname = trimToNull(nickname);
        if (normalizedNickname != null && !normalizedNickname.equals(user.getNickname())) {
            user.setNickname(normalizedNickname); changed = true;
        }
        String normalizedAvatar = trimToNull(avatarUrl);
        if (normalizedAvatar != null && !normalizedAvatar.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(normalizedAvatar); changed = true;
        }
        return changed;
    }

    private void ensureExistingTeacherHasProfile(User user) {
        if (user.isTeacher() && userRepository.findTeacherProfileByUserId(user.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_NOT_ALLOWED, "教师资料尚未完成，请联系管理员");
        }
    }

    private Map<String, Object> issueTokens(User user) {
        CurrentUser currentUser = getCurrentUser(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole(),
                currentUser.getPhone(), currentUser.getProfileId(), currentUser.getProfileType()));
        result.put("refreshToken", jwtService.generateRefreshToken(
                user.getId(), user.getUsername(), tokenVersion(user)));
        result.put("userId", user.getId()); result.put("username", user.getUsername());
        result.put("phone", user.getPhone()); result.put("role", user.getRole());
        result.put("profileId", currentUser.getProfileId()); result.put("profileType", currentUser.getProfileType());
        result.put("profileComplete", currentUser.getProfileId() != null);
        result.put("wechatBound", isWechatBound(user));
        result.put("nickname", user.getNickname()); result.put("avatar", user.getAvatarUrl());
        return result;
    }

    private int tokenVersion(User user) {
        return user.getTokenVersion() == null ? 0 : user.getTokenVersion();
    }

    private void validateWechatRole(String role) {
        if (!"TEACHER".equals(role) && !"PARENT".equals(role)) {
            throw new BusinessException(ErrorCode.PARAM_VALID_ERROR, "role必须是TEACHER或PARENT");
        }
    }
    private void ensureEnabled(User user) {
        if (!user.isEnabled()) throw new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED);
    }
    private void updateLastLogin(User user) {
        try { userRepository.updateLastLoginAt(user.getId()); }
        catch (Exception e) { log.warn("Failed to update lastLoginAt for user {}: {}", user.getId(), e.getMessage()); }
    }
    private static boolean isWechatBound(User user) {
        return user.getWechatOpenid() != null && !user.getWechatOpenid().isBlank();
    }
    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
