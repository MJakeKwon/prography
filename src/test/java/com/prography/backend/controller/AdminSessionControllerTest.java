package com.prography.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.domain.session.controller.AdminSessionController;
import com.prography.backend.domain.session.entity.QRCode;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.entity.SessionStatus;
import com.prography.backend.domain.session.service.SessionAdminService;
import com.prography.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSessionController.class)
@Import(GlobalExceptionHandler.class)
class AdminSessionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    SessionAdminService sessionAdminService;

    @Test
    @DisplayName("일정 생성 성공")
    void create_session_success() throws Exception {
        String requestJson = """
                {
                  "title": "정기 세션 1",
                  "sessionDate": "2026-03-01",
                  "sessionTime": "19:00:00",
                  "location": "강남",
                  "status": "SCHEDULED"
                }
                """;

        Cohort cohort = Cohort.builder().generation(11).name("11기").build();
        setField(cohort, "id", 11L);

        Session session = Session.builder()
                .cohort(cohort)
                .title("정기 세션 1")
                .sessionDate(LocalDate.of(2026, 3, 1))
                .sessionTime(LocalTime.of(19, 0))
                .location("강남")
                .status(SessionStatus.SCHEDULED)
                .build();
        setField(session, "id", 100L);

        given(sessionAdminService.createSession(
                eq("정기 세션 1"),
                eq(LocalDate.of(2026, 3, 1)),
                eq(LocalTime.of(19, 0)),
                eq("강남"),
                eq(SessionStatus.SCHEDULED)
        )).willReturn(session);

        mockMvc.perform(post("/admin/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.title").value("정기 세션 1"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("QR 생성 성공")
    void create_qrcode_success() throws Exception {
        Session session = Session.builder()
                .cohort(Cohort.builder().generation(11).name("11기").build())
                .title("세션")
                .sessionDate(LocalDate.of(2026, 3, 1))
                .sessionTime(LocalTime.of(19, 0))
                .location("강남")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        setField(session, "id", 100L);

        QRCode qrCode = QRCode.builder()
                .session(session)
                .hashValue("uuid-hash")
                .expiresAt(LocalDateTime.of(2026, 3, 2, 19, 0))
                .revokedAt(null)
                .build();
        setField(qrCode, "id", 1000L);

        given(sessionAdminService.createQRCode(100L)).willReturn(qrCode);

        mockMvc.perform(post("/admin/sessions/100/qrcodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1000))
                .andExpect(jsonPath("$.data.sessionId").value(100))
                .andExpect(jsonPath("$.data.hashValue").value("uuid-hash"));
    }

    @Test
    @DisplayName("QR 갱신 성공")
    void refresh_qrcode_success() throws Exception {
        Session session = Session.builder()
                .cohort(Cohort.builder().generation(11).name("11기").build())
                .title("세션")
                .sessionDate(LocalDate.now())
                .sessionTime(LocalTime.now())
                .location("강남")
                .status(SessionStatus.IN_PROGRESS)
                .build();
        setField(session, "id", 100L);

        QRCode qrCode = QRCode.builder()
                .session(session)
                .hashValue("new-hash")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .revokedAt(null)
                .build();
        setField(qrCode, "id", 2000L);

        given(sessionAdminService.refreshQRCode(300L)).willReturn(qrCode);

        mockMvc.perform(put("/admin/qrcodes/300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2000))
                .andExpect(jsonPath("$.data.hashValue").value("new-hash"));
    }

    private void setField(Object target, String fieldName, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, fieldName, value);
    }
}