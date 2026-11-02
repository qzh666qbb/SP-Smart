package com.example.training.auth.controller;

import com.example.training.auth.domain.service.AuthenticationService;
import com.example.training.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerWechatLoginWebTest {

    @Mock
    private AuthenticationService authenticationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthenticationController(authenticationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void invalidWechatRoleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "code-invalid-role",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.data.role").value("role必须是TEACHER或PARENT"));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void missingWechatRoleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "code-missing-role"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(10001))
                .andExpect(jsonPath("$.data.role").value("role不能为空"));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void rawOpenidCannotBeUsedInsteadOfWechatCode() throws Exception {
        mockMvc.perform(post("/api/auth/login/wechat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "openid": "forged-openid",
                                  "role": "PARENT"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.code").value("微信code不能为空"));

        verifyNoInteractions(authenticationService);
    }
    @Test
    void oldClientCanSubmitOpaqueTicketWithoutRole() throws Exception {
        String ticket = "compat_0123456789abcdef0123456789abcdef";
        org.mockito.Mockito.when(authenticationService.loginByWechat(ticket, "TEACHER", null, null))
                .thenReturn(java.util.Map.of("accessToken", "verified-token"));
        mockMvc.perform(post("/api/auth/login/wechat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"openid\":\"" + ticket + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.accessToken").value("verified-token"));
    }
}
