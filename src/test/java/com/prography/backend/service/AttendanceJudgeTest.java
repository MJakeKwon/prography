package com.prography.backend.service;

import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.attendance.service.AttendanceJudge;
import com.prography.backend.domain.session.entity.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceJudgeTest {

    private final AttendanceJudge attendanceJudge = new AttendanceJudge();

    @Test
    @DisplayName("일정 시작 시각 이전이면 PRESENT")
    void judge_present_before_start() {
        Session session = session(
                LocalDate.of(2026, 2, 26),
                LocalTime.of(19, 0)
        );

        LocalDateTime now = LocalDateTime.of(2026, 2, 26, 18, 59);

        AttendanceStatus status = attendanceJudge.judgeStatus(session, now);

        assertThat(status).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("일정 시작 시각 이후면 LATE")
    void judge_late_after_start() {
        Session session = session(
                LocalDate.of(2026, 2, 26),
                LocalTime.of(19, 0)
        );

        LocalDateTime now = LocalDateTime.of(2026, 2, 26, 19, 1);

        AttendanceStatus status = attendanceJudge.judgeStatus(session, now);

        assertThat(status).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    @DisplayName("시작 전 지각분은 0분")
    void late_minutes_before_start_zero() {
        Session session = session(
                LocalDate.of(2026, 2, 26),
                LocalTime.of(19, 0)
        );

        LocalDateTime now = LocalDateTime.of(2026, 2, 26, 18, 30);

        int minutes = attendanceJudge.calculateLateMinutes(session, now);

        assertThat(minutes).isZero();
    }

    @Test
    @DisplayName("시작 후 지각분 계산")
    void late_minutes_after_start() {
        Session session = session(
                LocalDate.of(2026, 2, 26),
                LocalTime.of(19, 0)
        );

        LocalDateTime now = LocalDateTime.of(2026, 2, 26, 19, 17);

        int minutes = attendanceJudge.calculateLateMinutes(session, now);

        assertThat(minutes).isEqualTo(17);
    }

    private Session session(LocalDate date, LocalTime time) {
        // 네 Session 엔티티 builder/생성자에 맞춰 수정 필요
        return Session.builder()
                .title("테스트 세션")
                .sessionDate(date)
                .sessionTime(time)
                .location("강남")
                .build();
    }
}