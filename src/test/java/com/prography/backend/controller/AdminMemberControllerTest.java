package com.prography.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.backend.domain.member.controller.AdminMemberController;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.entity.Role;
import com.prography.backend.domain.member.service.MemberAdminService;
import com.prography.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMemberController.class)
@Import(GlobalExceptionHandler.class)
class AdminMemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean MemberAdminService memberAdminService;

    @Test
    @DisplayName("회원 등록 성공")
    void create_member_success() throws Exception {
        String requestJson = """
                {
                  "loginId": "user1",
                  "password": "pw1234",
                  "name": "홍길동",
                  "phone": "010-1234-5678",
                  "role": "MEMBER",
                  "cohortId": 2,
                  "partId": 7,
                  "teamId": 1
                }
                """;

        Member member = Member.builder()
                .loginId("user1")
                .passwordHash("hashed")
                .name("홍길동")
                .phone("010-1234-5678")
                .status(MemberStatus.ACTIVE)
                .role(Role.MEMBER)
                .build();
        setField(member, "id", 10L);

        given(memberAdminService.createMember(
                eq("user1"), eq("pw1234"), eq("홍길동"), eq("010-1234-5678"),
                eq(Role.MEMBER), eq(2L), eq(7L), eq(1L)
        )).willReturn(member);

        mockMvc.perform(post("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.loginId").value("user1"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
    }

    @Test
    @DisplayName("회원 대시보드 조회 성공")
    void get_member_dashboard_success() throws Exception {
        Member m1 = Member.builder().loginId("u1").passwordHash("h").name("A").phone("010").status(MemberStatus.ACTIVE).role(Role.MEMBER).build();
        Member m2 = Member.builder().loginId("u2").passwordHash("h").name("B").phone("010").status(MemberStatus.ACTIVE).role(Role.MEMBER).build();
        setField(m1, "id", 1L);
        setField(m2, "id", 2L);

        given(memberAdminService.getMemberDashboard()).willReturn(List.of(m1, m2));

        mockMvc.perform(get("/admin/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[1].id").value(2L));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_member_success() throws Exception {
        doNothing().when(memberAdminService).withdrawMember(5L);

        mockMvc.perform(delete("/admin/members/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist()); // null 직렬화 설정에 따라 조정 필요
    }

    private void setField(Object target, String fieldName, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, fieldName, value);
    }
}