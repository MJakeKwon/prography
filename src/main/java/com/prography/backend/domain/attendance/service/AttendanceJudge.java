package com.prography.backend.domain.attendance.service;

import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.session.entity.Session;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AttendanceJudge {

    public AttendanceStatus judgeStatus(Session session, LocalDateTime now) {
        return now.isAfter(session.getStartDateTime()) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
    }

    public int calculateLateMinutes(Session session, LocalDateTime now) {
        if (!now.isAfter(session.getStartDateTime())) {
            return 0;
        }
        long minutes = Duration.between(session.getStartDateTime(), now).toMinutes();
        return (int) Math.max(minutes, 0);
    }
}