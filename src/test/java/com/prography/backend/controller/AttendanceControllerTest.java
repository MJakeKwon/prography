package com.prography.backend.controller;

import com.prography.backend.domain.attendance.controller.AttendanceController;
import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.attendance.service.AttendanceCommandService;
import com.prography.backend.domain.attendance.service.AttendanceQueryService;
import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.entity.Role;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.entity.SessionStatus;
import com.prography.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@Import(GlobalExceptionHandler.class)
class AttendanceControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AttendanceCommandService attendanceCommandService;
    @MockitoBean AttendanceQueryService attendanceQueryService;

    @Test
    @DisplayName("QR 출석 체크 성공")
    void check_in_by_qr_success() throws Exception {
        Cohort cohort = Cohort.builder().generation(11).name("11기").build();
        setField(cohort, "id", 11L);

        Session session = Session.builder()
                .cohort(cohort)
                .title("세션")
                .sessionDate(LocalDate.of(2026, 2, 26))
                .sessionTime(LocalTime.of(19, 0))
                .location("강남")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        setField(session, "id", 101L);

        Member member = Member.builder()
                .loginId("u1")
                .passwordHash("h")
                .name("홍길동")
                .phone("010")
                .status(MemberStatus.ACTIVE)
                .role(Role.MEMBER)
                .build();
        setField(member, "id", 1L);

        Attendance attendance = Attendance.builder()
                .session(session)
                .member(member)
                .qrcode(null)
                .status(AttendanceStatus.PRESENT)
                .lateMinutes(0)
                .penaltyAmount(0)
                .reason(null)
                .checkedInAt(LocalDateTime.of(2026, 2, 26, 18, 55))
                .build();
        setField(attendance, "id", 999L);

        given(attendanceCommandService.checkInByQr("hash-123", 1L)).willReturn(attendance);

        String req = """
                {
                  "hashValue": "hash-123",
                  "memberId": 1
                }
                """;

        mockMvc.perform(post("/attendances")
                        .contentType(APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(999))
                .andExpect(jsonPath("$.data.sessionId").value(101))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.status").value("PRESENT"));
    }

    @Test
    @DisplayName("내 출결 기록 조회 성공")
    void get_my_attendances_success() throws Exception {
        Cohort cohort = Cohort.builder().generation(11).name("11기").build();
        setField(cohort, "id", 11L);

        Session session = Session.builder()
                .cohort(cohort)
                .title("세션")
                .sessionDate(LocalDate.of(2026, 2, 26))
                .sessionTime(LocalTime.of(19, 0))
                .location("강남")
                .status(SessionStatus.COMPLETED)
                .build();
        setField(session, "id", 101L);

        Member member = Member.builder()
                .loginId("u1")
                .passwordHash("h")
                .name("홍길동")
                .phone("010")
                .status(MemberStatus.ACTIVE)
                .role(Role.MEMBER)
                .build();
        setField(member, "id", 1L);

        Attendance a1 = Attendance.builder()
                .session(session)
                .member(member)
                .qrcode(null)
                .status(AttendanceStatus.LATE)
                .lateMinutes(3)
                .penaltyAmount(1500)
                .reason(null)
                .checkedInAt(LocalDateTime.of(2026, 2, 26, 19, 3))
                .build();
        setField(a1, "id", 10L);

        given(attendanceQueryService.getMyAttendances(1L)).willReturn(List.of(a1));

        mockMvc.perform(get("/attendances").param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("LATE"))
                .andExpect(jsonPath("$.data[0].lateMinutes").value(3))
                .andExpect(jsonPath("$.data[0].penaltyAmount").value(1500));
    }

    private void setField(Object target, String fieldName, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, fieldName, value);
    }
}