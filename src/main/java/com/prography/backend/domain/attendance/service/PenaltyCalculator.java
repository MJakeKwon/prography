package com.prography.backend.domain.attendance.service;

import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import org.springframework.stereotype.Component;

@Component
public class PenaltyCalculator {

    public static final int ABSENT_PENALTY = 10_000;
    public static final int LATE_UNIT = 500;
    public static final int MAX_PENALTY = 10_000;

    public int calculate(AttendanceStatus status, int lateMinutes) {
        return switch (status) {
            case PRESENT, EXCUSED -> 0;
            case ABSENT -> ABSENT_PENALTY;
            case LATE -> Math.min(Math.max(lateMinutes, 0) * LATE_UNIT, MAX_PENALTY);
        };
    }
}