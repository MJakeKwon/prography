package com.prography.backend.controller;

import com.prography.backend.domain.attendance.controller.AdminAttendanceController;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAttendanceController.class)
@Import(GlobalExceptionHandler.class)
class AdminAttendanceControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AttendanceCommandService attendanceCommandService;
    @MockitoBean AttendanceQueryService attendanceQueryService;

    @Test
    @DisplayName("관리자 출결 등록 성공")
    void admin_create_attendance_success() throws Exception {
        Attendance attendance = attendanceFixture(1L, AttendanceStatus.ABSENT, 0, 10_000);

        given(attendanceCommandService.adminCreateAttendance(
                101L, 1L, AttendanceStatus.ABSENT, "무단 결석", null
        )).willReturn(attendance);

        String req = """
                {
                  "sessionId": 101,
                  "memberId": 1,
                  "status": "ABSENT",
                  "reason": "무단 결석"
                }
                """;

        mockMvc.perform(post("/admin/attendances")
                        .contentType(APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ABSENT"))
                .andExpect(jsonPath("$.data.penaltyAmount").value(10000));
    }

    @Test
    @DisplayName("관리자 출결 수정 성공")
    void admin_update_attendance_success() throws Exception {
        Attendance attendance = attendanceFixture(2L, AttendanceStatus.EXCUSED, 0, 0);

        given(attendanceCommandService.adminUpdateAttendance(
                2L, AttendanceStatus.EXCUSED, "병원", null
        )).willReturn(attendance);

        String req = """
                {
                  "status": "EXCUSED",
                  "reason": "병원"
                }
                """;

        mockMvc.perform(put("/admin/attendances/2")
                        .contentType(APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.status").value("EXCUSED"))
                .andExpect(jsonPath("$.data.penaltyAmount").value(0));
    }

    private Attendance attendanceFixture(Long attendanceId, AttendanceStatus status, int lateMinutes, int penalty) {
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

        Attendance attendance = Attendance.builder()
                .session(session)
                .member(member)
                .qrcode(null)
                .status(status)
                .lateMinutes(lateMinutes)
                .penaltyAmount(penalty)
                .reason("사유")
                .checkedInAt(LocalDateTime.of(2026, 2, 26, 19, 10))
                .build();
        setField(attendance, "id", attendanceId);
        return attendance;
    }

    private void setField(Object target, String fieldName, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, fieldName, value);
    }
}