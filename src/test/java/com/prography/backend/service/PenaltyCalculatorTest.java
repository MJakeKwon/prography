package com.prography.backend.service;

import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.attendance.service.PenaltyCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PenaltyCalculatorTest {

    private final PenaltyCalculator penaltyCalculator = new PenaltyCalculator();

    @Test
    @DisplayName("PRESENT 패널티는 0원")
    void calculate_present_zero() {
        int penalty = penaltyCalculator.calculate(AttendanceStatus.PRESENT, 0);
        assertThat(penalty).isZero();
    }

    @Test
    @DisplayName("EXCUSED 패널티는 0원")
    void calculate_excused_zero() {
        int penalty = penaltyCalculator.calculate(AttendanceStatus.EXCUSED, 10);
        assertThat(penalty).isZero();
    }

    @Test
    @DisplayName("ABSENT 패널티는 10000원")
    void calculate_absent_fixed_10000() {
        int penalty = penaltyCalculator.calculate(AttendanceStatus.ABSENT, 0);
        assertThat(penalty).isEqualTo(10_000);
    }

    @Test
    @DisplayName("LATE 패널티는 지각분 * 500")
    void calculate_late_penalty() {
        int penalty = penaltyCalculator.calculate(AttendanceStatus.LATE, 7);
        assertThat(penalty).isEqualTo(3_500);
    }

    @Test
    @DisplayName("LATE 패널티는 최대 10000원으로 캡핑")
    void calculate_late_penalty_capped() {
        int penalty = penaltyCalculator.calculate(AttendanceStatus.LATE, 30); // 15000 -> cap 10000
        assertThat(penalty).isEqualTo(10_000);
    }

    @Test
    @DisplayName("LATE 지각분이 음수면 0으로 처리")
    void calculate_late_negative_minutes_zero() {
        int penalty = penaltyCalculator.calculate(AttendanceStatus.LATE, -5);
        assertThat(penalty).isZero();
    }
}