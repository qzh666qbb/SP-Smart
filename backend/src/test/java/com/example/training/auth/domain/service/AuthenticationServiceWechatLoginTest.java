package com.example.training.auth.domain.service;

import com.example.training.auth.domain.model.ParentProfile;
import com.example.training.auth.domain.model.TeacherProfile;
import com.example.training.auth.domain.model.User;
import com.example.training.auth.domain.repository.UserRepository;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.teacher.domain.model.TeacherApply;
import com.example.training.teacher.domain.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceWechatLoginTest {
    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TeacherRepository teacherRepository;
    @Mock WechatGateway wechatGateway;
    @Mock LegacyWechatTicketService legacyWechatTickets;

    AuthenticationService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticationService(userRepository, jwtService, passwordEncoder,
                teacherRepository, wechatGateway, legacyWechatTickets);
    }

    @Test
    void newWechatParentIsCreatedButRequiresProfileCompletion() {
        WechatGateway.WechatIdentity identity = identity("parent-new");
        when(wechatGateway.exchangeCode("code-parent")).thenReturn(identity);
        arrangeIdentityEmpty(identity);
        AtomicReference<User> saved = arrangeSaveWithId(6L);

        var result = service.loginByWechat("code-parent", "PARENT", "微信家长", "https://avatar");

        assertEquals("PARENT", result.get("role"));
        assertEquals(false, result.get("profileComplete"));
        assertEquals(true, result.get("wechatBound"));
        assertEquals("parent-new", saved.get().getWechatOpenid());
        assertNull(result.get("profileId"));
    }

    @Test
    void pendingTeacherCannotReceiveToken() {
        WechatGateway.WechatIdentity identity = identity("teacher-pending");
        when(wechatGateway.exchangeCode("code-pending")).thenReturn(identity);
        arrangeIdentityEmpty(identity);
        when(teacherRepository.findApplyByOpenid(identity.openid())).thenReturn(Optional.of(
                TeacherApply.builder().phone("13111133311").status("PENDING").build()));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.loginByWechat("code-pending", "TEACHER", null, null));

        assertEquals(ErrorCode.TEACHER_APPLY_PENDING, error.getErrorCode());
        verify(jwtService, never()).generateAccessToken(any(), any(), any(), any(), any(), any());
    }

    @Test
    void approvedTeacherUsesReviewedAccountAndProfile() {
        WechatGateway.WechatIdentity identity = identity("teacher-approved");
        User teacher = enabledUser(9L, null, "TEACHER");
        when(wechatGateway.exchangeCode("code-approved")).thenReturn(identity);
        arrangeIdentityEmpty(identity);
        when(teacherRepository.findApplyByOpenid(identity.openid())).thenReturn(Optional.of(
                TeacherApply.builder().phone("13900000000").status("APPROVED").build()));
        when(userRepository.findByPhone("13900000000")).thenReturn(Optional.of(teacher));
        when(userRepository.findTeacherProfileByUserId(9L)).thenReturn(Optional.of(
                TeacherProfile.builder().id(19L).userId(9L).build()));
        when(userRepository.save(teacher)).thenReturn(teacher);
        when(userRepository.findById(9L)).thenReturn(Optional.of(teacher));
        arrangeTokens();

        var result = service.loginByWechat("code-approved", "TEACHER", null, null);

        assertEquals(9L, result.get("userId"));
        assertEquals(19L, result.get("profileId"));
        assertEquals(identity.openid(), teacher.getWechatOpenid());
    }

    @Test
    void passwordAccountCanBindWechatWithoutCreatingAnotherUser() {
        WechatGateway.WechatIdentity identity = identity("bind-id6");
        User parent = enabledUser(6L, null, "PARENT");
        when(userRepository.findById(6L)).thenReturn(Optional.of(parent));
        when(wechatGateway.exchangeCode("bind-code")).thenReturn(identity);
        arrangeIdentityEmpty(identity);
        when(userRepository.save(parent)).thenReturn(parent);
        when(userRepository.findParentProfileByUserId(6L)).thenReturn(Optional.of(
                ParentProfile.builder().id(1L).userId(6L).build()));
        arrangeTokens();

        var result = service.bindWechat(6L, "bind-code", "家长昵称", "https://avatar");

        assertEquals(6L, result.get("userId"));
        assertEquals(true, result.get("wechatBound"));
        assertEquals(identity.openid(), parent.getWechatOpenid());
        verify(userRepository).save(parent);
    }

    @Test
    void bindingWechatOwnedByAnotherAccountIsRejected() {
        WechatGateway.WechatIdentity identity = identity("occupied");
        User current = enabledUser(6L, null, "PARENT");
        User owner = enabledUser(5L, identity.openid(), "PARENT");
        when(userRepository.findById(6L)).thenReturn(Optional.of(current));
        when(wechatGateway.exchangeCode("bind-code")).thenReturn(identity);
        when(userRepository.findByWechatOpenid(identity.openid())).thenReturn(Optional.of(owner));
        when(userRepository.findByWechatUnionid(identity.unionid())).thenReturn(Optional.of(owner));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.bindWechat(6L, "bind-code", null, null));

        assertEquals(ErrorCode.OPERATION_NOT_ALLOWED, error.getErrorCode());
        verify(userRepository, never()).save(current);
    }

    @Test
    void completingParentProfileCreatesBusinessProfileAndReturnsNewProfileId() {
        User parent = enabledUser(6L, "bound", "PARENT");
        when(userRepository.findById(6L)).thenReturn(Optional.of(parent));
        when(userRepository.findByPhone("13111133311")).thenReturn(Optional.empty());
        when(userRepository.findParentProfileByUserId(6L)).thenReturn(Optional.empty(), Optional.of(
                ParentProfile.builder().id(11L).userId(6L).name("测试家长").phone("13111133311").build()));
        when(userRepository.saveParentProfile(any())).thenAnswer(invocation -> {
            ParentProfile profile = invocation.getArgument(0); profile.setId(11L); return profile;
        });
        when(userRepository.save(parent)).thenReturn(parent);
        arrangeTokens();

        var result = service.completeParentProfile(6L, "测试家长", "13111133311", "MOTHER");

        assertEquals(11L, result.get("profileId"));
        assertEquals(true, result.get("profileComplete"));
    }

    @Test
    void invalidRoleIsRejectedBeforeWechatExchange() {
        BusinessException error = assertThrows(BusinessException.class,
                () -> service.loginByWechat("code", "ADMIN", null, null));
        assertEquals(ErrorCode.PARAM_VALID_ERROR, error.getErrorCode());
        verifyNoInteractions(wechatGateway);
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.refreshToken("access-token"));

        assertEquals(ErrorCode.AUTH_INVALID_TOKEN, error.getErrorCode());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void disabledAccountCannotRefreshToken() {
        User disabled = User.builder().id(8L).username("disabled").role("PARENT").status("DISABLED").build();
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(false);
        when(jwtService.extractUserId("refresh-token")).thenReturn(8L);
        when(userRepository.findById(8L)).thenReturn(Optional.of(disabled));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.refreshToken("refresh-token"));

        assertEquals(ErrorCode.AUTH_ACCOUNT_DISABLED, error.getErrorCode());
    }

    @Test
    void refreshTokenIsRejectedAfterTokenVersionChanges() {
        User user = enabledUser(8L, null, "PARENT");
        user.setTokenVersion(2);
        when(jwtService.isRefreshToken("old-refresh-token")).thenReturn(true);
        when(jwtService.isTokenExpired("old-refresh-token")).thenReturn(false);
        when(jwtService.extractUserId("old-refresh-token")).thenReturn(8L);
        when(jwtService.extractTokenVersion("old-refresh-token")).thenReturn(1);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.refreshToken("old-refresh-token"));

        assertEquals(ErrorCode.AUTH_INVALID_TOKEN, error.getErrorCode());
        verify(jwtService, never()).generateAccessToken(any(), any(), any(), any(), any(), any());
    }

    @Test
    void logoutIncrementsTokenVersion() {
        User user = enabledUser(8L, null, "PARENT");
        user.setTokenVersion(3);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.logout(8L);

        assertEquals(4, user.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    void passwordLoginAcceptsTeacherPhone() {
        User teacher = User.builder().id(12L).username("legacy-teacher")
                .phone("13800138000").passwordHash("encoded")
                .role("TEACHER").status("ENABLED").build();
        when(userRepository.findByUsername("13800138000")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("13800138000")).thenReturn(Optional.of(teacher));
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(userRepository.findById(12L)).thenReturn(Optional.of(teacher));
        when(userRepository.findTeacherProfileByUserId(12L)).thenReturn(Optional.of(
                TeacherProfile.builder().id(22L).userId(12L).name("张老师").build()));
        arrangeTokens();

        var result = service.login("13800138000", "123456");

        assertEquals(12L, result.get("userId"));
        assertEquals(22L, result.get("profileId"));
        verify(userRepository).updateLastLoginAt(12L);
    }

    @Test
    void legacyTicketUsesVerifiedAccountRoleInsteadOfClientRole() {
        String ticket = "compat_0123456789abcdef0123456789abcdef";
        var identity = identity("existing-parent");
        User parent = enabledUser(8L, identity.openid(), "PARENT");
        when(legacyWechatTickets.consume(ticket)).thenReturn(identity);
        when(userRepository.findByWechatOpenid(identity.openid())).thenReturn(Optional.of(parent));
        when(userRepository.findById(8L)).thenReturn(Optional.of(parent));
        arrangeTokens();
        var result = service.loginByWechat(ticket, "TEACHER", null, null);
        assertEquals("PARENT", result.get("role"));
        verifyNoInteractions(wechatGateway);
    }

    @Test
    void legacyTicketCannotCreateUnapprovedAccount() {
        String ticket = "compat_0123456789abcdef0123456789abcdef";
        var identity = identity("unknown");
        when(legacyWechatTickets.consume(ticket)).thenReturn(identity);
        arrangeIdentityEmpty(identity);
        assertThrows(BusinessException.class, () -> service.loginByWechat(ticket, "TEACHER", null, null));
        verify(userRepository, never()).save(any());
    }

    private WechatGateway.WechatIdentity identity(String value) {
        return new WechatGateway.WechatIdentity(value, "union-" + value);
    }
    private void arrangeIdentityEmpty(WechatGateway.WechatIdentity identity) {
        when(userRepository.findByWechatOpenid(identity.openid())).thenReturn(Optional.empty());
        when(userRepository.findByWechatUnionid(identity.unionid())).thenReturn(Optional.empty());
    }
    private AtomicReference<User> arrangeSaveWithId(long id) {
        AtomicReference<User> saved = new AtomicReference<>();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0); user.setId(id); saved.set(user); return user;
        });
        when(userRepository.findById(id)).thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        arrangeTokens();
        return saved;
    }
    private void arrangeTokens() {
        when(jwtService.generateAccessToken(any(), any(), any(), any(), any(), any())).thenReturn("access");
        when(jwtService.generateRefreshToken(any(), any(), anyInt())).thenReturn("refresh");
    }
    private User enabledUser(Long id, String openid, String role) {
        return User.builder().id(id).username("user_" + id).role(role)
                .wechatOpenid(openid).status("ENABLED").build();
    }
}
