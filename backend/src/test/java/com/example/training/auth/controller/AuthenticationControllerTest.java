package com.example.training.auth.controller;

import com.example.training.auth.application.dto.LoginRequest;
import com.example.training.auth.application.dto.RefreshTokenRequest;
import com.example.training.auth.application.dto.WechatLoginRequest;
import com.example.training.auth.domain.service.AuthenticationService;
import com.example.training.common.exception.BusinessException;
import com.example.training.common.exception.ErrorCode;
import com.example.training.common.response.ApiResponse;
import com.example.training.common.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private Map<String, Object> loginResult;

    @BeforeEach
    void setUp() {
        loginResult = new HashMap<>();
        loginResult.put("accessToken", "access-token-123");
        loginResult.put("refreshToken", "refresh-token-456");
        loginResult.put("userId", 1L);
        loginResult.put("username", "admin");
        loginResult.put("role", "ADMIN");
        loginResult.put("profileId", null);
        loginResult.put("profileType", null);
    }

    @Nested
    @DisplayName("POST /api/auth/login 登录")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 返回 accessToken、refreshToken、用户信息")
        void login_Success_ReturnsTokensAndUserInfo() {
            LoginRequest req = LoginRequest.builder().username("admin").password("123456").build();
            when(authenticationService.login("admin", "123456")).thenReturn(loginResult);

            ApiResponse<Map<String, Object>> resp = authenticationController.login(req);

            assertNotNull(resp);
            assertEquals(0, resp.getCode());
            assertEquals("success", resp.getMessage());
            assertNotNull(resp.getData());
            assertEquals("access-token-123", resp.getData().get("accessToken"));
            assertEquals("refresh-token-456", resp.getData().get("refreshToken"));
            assertEquals(1L, resp.getData().get("userId"));
            assertEquals("admin", resp.getData().get("username"));
            assertEquals("ADMIN", resp.getData().get("role"));
            verify(authenticationService, times(1)).login("admin", "123456");
        }

        @Test
        @DisplayName("密码错误 - 抛出 AUTH_INVALID_CREDENTIALS")
        void login_WrongPassword_ThrowsBusinessException() {
            LoginRequest req = LoginRequest.builder().username("admin").password("wrong").build();
            when(authenticationService.login("admin", "wrong"))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    authenticationController.login(req));

            assertEquals(ErrorCode.AUTH_INVALID_CREDENTIALS, ex.getErrorCode());
        }

        @Test
        @DisplayName("账户已禁用 - 抛出 AUTH_ACCOUNT_DISABLED")
        void login_DisabledAccount_ThrowsBusinessException() {
            LoginRequest req = LoginRequest.builder().username("disabled").password("123456").build();
            when(authenticationService.login("disabled", "123456"))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_ACCOUNT_DISABLED));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    authenticationController.login(req));

            assertEquals(ErrorCode.AUTH_ACCOUNT_DISABLED, ex.getErrorCode());
        }

        @Test
        @DisplayName("教师登录成功 - 返回正确 TEACHER 角色信息")
        void login_TeacherRole_ReturnsTeacherInfo() {
            Map<String, Object> teacherResult = new HashMap<>();
            teacherResult.put("accessToken", "t-access");
            teacherResult.put("refreshToken", "t-refresh");
            teacherResult.put("userId", 21L);
            teacherResult.put("username", "teacher_new_01");
            teacherResult.put("role", "TEACHER");
            teacherResult.put("profileId", 21L);
            teacherResult.put("profileType", "TEACHER");
            LoginRequest req = LoginRequest.builder().username("teacher_new_01").password("123456").build();
            when(authenticationService.login("teacher_new_01", "123456")).thenReturn(teacherResult);

            ApiResponse<Map<String, Object>> resp = authenticationController.login(req);

            assertEquals(0, resp.getCode());
            assertEquals(21L, resp.getData().get("userId"));
            assertEquals("TEACHER", resp.getData().get("role"));
            assertEquals(21L, resp.getData().get("profileId"));
            assertEquals("TEACHER", resp.getData().get("profileType"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login/wechat 微信登录")
    class WechatLoginTests {

        @Test
        @DisplayName("微信登录成功 - 返回 JWT 和用户信息")
        void loginByWechat_Success_ReturnsTokens() {
            Map<String, Object> wxResult = new HashMap<>(loginResult);
            wxResult.put("role", "PARENT");
            wxResult.put("username", "wx_user");
            WechatLoginRequest req = WechatLoginRequest.builder()
                    .openid("o-test-openid")
                    .unionid("u-test-unionid")
                    .build();
            when(authenticationService.loginByWechat("o-test-openid", "u-test-unionid")).thenReturn(wxResult);

            ApiResponse<Map<String, Object>> resp = authenticationController.loginByWechat(req);

            assertEquals(0, resp.getCode());
            assertEquals("PARENT", resp.getData().get("role"));
            verify(authenticationService).loginByWechat("o-test-openid", "u-test-unionid");
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh 刷新令牌")
    class RefreshTokenTests {

        @Test
        @DisplayName("刷新令牌成功 - 返回新的 accessToken 和 refreshToken")
        void refreshToken_Success_ReturnsNewTokens() {
            Map<String, Object> refreshResult = new HashMap<>();
            refreshResult.put("accessToken", "new-access-token");
            refreshResult.put("refreshToken", "new-refresh-token");
            refreshResult.put("profileId", 21L);
            refreshResult.put("profileType", "TEACHER");
            RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("old-refresh-abc").build();
            when(authenticationService.refreshToken("old-refresh-abc")).thenReturn(refreshResult);

            ApiResponse<Map<String, Object>> resp = authenticationController.refreshToken(req);

            assertEquals(0, resp.getCode());
            assertEquals("new-access-token", resp.getData().get("accessToken"));
            assertEquals("new-refresh-token", resp.getData().get("refreshToken"));
            assertEquals(21L, resp.getData().get("profileId"));
            verify(authenticationService).refreshToken("old-refresh-abc");
        }

        @Test
        @DisplayName("刷新令牌失败 - 无效 Token 抛出 AUTH_INVALID_TOKEN")
        void refreshToken_InvalidToken_ThrowsException() {
            RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("expired-token").build();
            when(authenticationService.refreshToken("expired-token"))
                    .thenThrow(new BusinessException(ErrorCode.AUTH_INVALID_TOKEN, "无效的刷新令牌"));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    authenticationController.refreshToken(req));

            assertEquals(ErrorCode.AUTH_INVALID_TOKEN, ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me 当前用户信息")
    class GetCurrentUserTests {

        @Test
        @DisplayName("已登录 - 返回当前用户完整信息")
        void getCurrentUser_LoggedIn_ReturnsAllFields() {
            CurrentUser user = CurrentUser.builder()
                    .userId(1L)
                    .username("admin")
                    .phone("13800000000")
                    .role("ADMIN")
                    .profileId(null)
                    .profileType(null)
                    .build();

            ApiResponse<Map<String, Object>> resp = authenticationController.getCurrentUser(user);

            assertEquals(0, resp.getCode());
            assertEquals(1L, resp.getData().get("userId"));
            assertEquals("admin", resp.getData().get("username"));
            assertEquals("13800000000", resp.getData().get("phone"));
            assertEquals("ADMIN", resp.getData().get("role"));
            assertNull(resp.getData().get("profileId"));
            assertNull(resp.getData().get("profileType"));
        }

        @Test
        @DisplayName("教师身份 - 返回 profileId 和 profileType")
        void getCurrentUser_TeacherRole_ReturnsTeacherProfile() {
            CurrentUser user = CurrentUser.builder()
                    .userId(21L)
                    .username("teacher_new_01")
                    .phone("15151395226")
                    .role("TEACHER")
                    .profileId(21L)
                    .profileType("TEACHER")
                    .build();

            ApiResponse<Map<String, Object>> resp = authenticationController.getCurrentUser(user);

            assertEquals(21L, resp.getData().get("profileId"));
            assertEquals("TEACHER", resp.getData().get("profileType"));
        }

        @Test
        @DisplayName("未登录 - currentUser 为 null 时抛出 AUTH_UNAUTHORIZED")
        void getCurrentUser_NullUser_ThrowsUnauthorized() {
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    authenticationController.getCurrentUser(null));

            assertEquals(ErrorCode.AUTH_UNAUTHORIZED, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("请先登录"));
        }
    }
}
