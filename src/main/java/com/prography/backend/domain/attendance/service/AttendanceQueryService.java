package com.prography.backend.domain.attendance.service;

import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.attendance.repository.AttendanceRepository;
import com.prography.backend.domain.cohort.repository.CohortMemberRepository;
import com.prography.backend.domain.deposit.entity.DepositHistory;
import com.prography.backend.domain.deposit.repository.DepositHistoryRepository;
import com.prography.backend.domain.session.repository.SessionRepository;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    // #18 내 출결 기록
    public List<Attendance> getMyAttendances(Long memberId) {
        return attendanceRepository.findAllByMemberIdOrderByIdDesc(memberId);
    }

    // #19 내 출결 요약
    public AttendanceSummary getAttendanceSummary(Long memberId) {
        List<Attendance> list = attendanceRepository.findAllByMemberIdOrderByIdDesc(memberId);
        return summarize(list);
    }

    // #22 일정별 출결 요약
    public AttendanceSummary getSessionAttendanceSummary(Long sessionId) {
        ensureSessionExists(sessionId);
        List<Attendance> list = attendanceRepository.findAllBySessionIdOrderByIdAsc(sessionId);
        return summarize(list);
    }

    // #23 회원 출결 상세 (관리자)
    public List<Attendance> getMemberAttendancesForAdmin(Long memberId) {
        return attendanceRepository.findAllByMemberIdOrderByIdDesc(memberId);
    }

    // #24 일정별 출결 목록
    public List<Attendance> getAttendancesBySession(Long sessionId) {
        ensureSessionExists(sessionId);
        return attendanceRepository.findAllBySessionIdOrderByIdAsc(sessionId);
    }

    // #25 보증금 이력
    public List<DepositHistory> getDepositHistories(Long cohortMemberId) {
        // 존재 확인 (없으면 COHORT_MEMBER_NOT_FOUND)
        cohortMemberRepository.findById(cohortMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        return depositHistoryRepository.findAllByCohortMemberIdOrderByIdDesc(cohortMemberId);
    }

    private void ensureSessionExists(Long sessionId) {
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
    }

    private AttendanceSummary summarize(List<Attendance> list) {
        Map<AttendanceStatus, Integer> counts = new EnumMap<>(AttendanceStatus.class);
        for (AttendanceStatus s : AttendanceStatus.values()) {
            counts.put(s, 0);
        }

        int totalPenalty = 0;
        for (Attendance a : list) {
            counts.put(a.getStatus(), counts.get(a.getStatus()) + 1);
            totalPenalty += a.getPenaltyAmount();
        }

        return new AttendanceSummary(
                list.size(),
                counts.get(AttendanceStatus.PRESENT),
                counts.get(AttendanceStatus.LATE),
                counts.get(AttendanceStatus.ABSENT),
                counts.get(AttendanceStatus.EXCUSED),
                totalPenalty
        );
    }

    @Getter
    public static class AttendanceSummary {
        private final int total;
        private final int present;
        private final int late;
        private final int absent;
        private final int excused;
        private final int totalPenalty;

        public AttendanceSummary(int total, int present, int late, int absent, int excused, int totalPenalty) {
            this.total = total;
            this.present = present;
            this.late = late;
            this.absent = absent;
            this.excused = excused;
            this.totalPenalty = totalPenalty;
        }
    }
}