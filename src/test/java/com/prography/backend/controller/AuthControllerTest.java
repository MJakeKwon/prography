package com.prography.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.backend.domain.member.controller.AuthController;
import com.prography.backend.domain.member.dto.LoginRequest;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.entity.Role;
import com.prography.backend.domain.member.service.AuthService;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import com.prography.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        // given
        LoginRequest request = new LoginRequest();
        setField(request, "loginId", "admin");
        setField(request, "password", "admin1234");

        Member member = Member.builder()
                .loginId("admin")
                .passwordHash("hashed")
                .name("관리자")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();
        setField(member, "id", 1L);

        given(authService.login("admin", "admin1234")).willReturn(member);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.memberId").value(1L))
                .andExpect(jsonPath("$.data.loginId").value("admin"))
                .andExpect(jsonPath("$.data.name").value("관리자"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("로그인 실패 - validation 에러")
    void login_fail_validation() throws Exception {
        String invalidJson = """
                {
                  "loginId": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("로그인 실패 - 비즈니스 예외")
    void login_fail_business_exception() throws Exception {
        LoginRequest request = new LoginRequest();
        setField(request, "loginId", "admin");
        setField(request, "password", "wrong");

        given(authService.login("admin", "wrong"))
                .willThrow(new BusinessException(ErrorCode.LOGIN_FAILED));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(ErrorCode.LOGIN_FAILED.getStatus().value()))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.LOGIN_FAILED.getCode()));
    }

    private void setField(Object target, String fieldName, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, fieldName, value);
    }
}