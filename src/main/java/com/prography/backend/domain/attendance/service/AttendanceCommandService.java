package com.prography.backend.domain.attendance.service;

import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.attendance.repository.AttendanceRepository;
import com.prography.backend.domain.cohort.entity.CohortMember;
import com.prography.backend.domain.cohort.repository.CohortMemberRepository;
import com.prography.backend.domain.deposit.entity.DepositHistory;
import com.prography.backend.domain.deposit.repository.DepositHistoryRepository;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.repository.MemberRepository;
import com.prography.backend.domain.session.entity.QRCode;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.repository.QRCodeRepository;
import com.prography.backend.domain.session.repository.SessionRepository;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceCommandService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final SessionRepository sessionRepository;
    private final QRCodeRepository qrCodeRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositHistoryRepository depositHistoryRepository;

    private final AttendanceJudge attendanceJudge;
    private final PenaltyCalculator penaltyCalculator;

    /**
     * API #17 QR 출석 체크
     * 검증 순서 요구사항 준수
     */
    @Transactional
    public Attendance checkInByQr(String hashValue, Long memberId) {
        LocalDateTime now = LocalDateTime.now();

        // 1) QR hash 유효성
        QRCode qrCode = qrCodeRepository.findByHashValue(hashValue)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_INVALID));

        // 2) QR 만료 여부
        if (qrCode.isRevoked() || qrCode.isExpired(now)) {
            throw new BusinessException(ErrorCode.QR_EXPIRED);
        }

        Session session = qrCode.getSession();

        // 3) 일정 상태 IN_PROGRESS 여부
        if (!session.isInProgress()) {
            throw new BusinessException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }

        // 4) 회원 존재 여부
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 5) 회원 탈퇴 여부
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }

        // 6) 중복 출결 여부
        if (attendanceRepository.existsBySessionIdAndMemberId(session.getId(), memberId)) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }

        // 7) 기수 회원 정보 존재 여부 + 보증금 차감 위해 락 조회
        CohortMember cohortMember = cohortMemberRepository.findByCohortIdAndMemberIdForUpdate(session.getCohort().getId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        AttendanceStatus status = attendanceJudge.judgeStatus(session, now);
        int lateMinutes = attendanceJudge.calculateLateMinutes(session, now);
        int penaltyAmount = penaltyCalculator.calculate(status, lateMinutes);

        Attendance attendance = Attendance.builder()
                .session(session)
                .member(member)
                .qrcode(qrCode)
                .status(status)
                .lateMinutes(lateMinutes)
                .penaltyAmount(penaltyAmount)
                .reason(null)
                .checkedInAt(now)
                .build();

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            // 유니크 (session_id, member_id) 최종 방어
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }

        if (penaltyAmount > 0) {
            applyPenalty(cohortMember, attendance, penaltyAmount, "QR 출석 체크 패널티");
        }

        return attendance;
    }

    /**
     * API #20 관리자 출결 등록
     */
    @Transactional
    public Attendance adminCreateAttendance(Long sessionId, Long memberId, AttendanceStatus status, String reason, LocalDateTime checkedInAt) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.MEMBER_WITHDRAWN);
        }

        if (attendanceRepository.existsBySessionIdAndMemberId(sessionId, memberId)) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }

        CohortMember cohortMember = cohortMemberRepository.findByCohortIdAndMemberIdForUpdate(session.getCohort().getId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        int lateMinutes = 0;
        if (status == AttendanceStatus.LATE) {
            LocalDateTime base = checkedInAt == null ? LocalDateTime.now() : checkedInAt;
            lateMinutes = attendanceJudge.calculateLateMinutes(session, base);
        }

        int penalty = penaltyCalculator.calculate(status, lateMinutes);

        // 공결 카운트 선반영 검증
        if (status == AttendanceStatus.EXCUSED) {
            try {
                cohortMember.increaseExcuseCount();
            } catch (IllegalStateException e) {
                throw new BusinessException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
            }
        }

        Attendance attendance = Attendance.builder()
                .session(session)
                .member(member)
                .qrcode(null)
                .status(status)
                .lateMinutes(lateMinutes)
                .penaltyAmount(penalty)
                .reason(reason)
                .checkedInAt(checkedInAt == null ? LocalDateTime.now() : checkedInAt)
                .build();

        try {
            attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
        }

        if (penalty > 0) {
            applyPenalty(cohortMember, attendance, penalty, "관리자 출결 등록 패널티");
        }

        return attendance;
    }

    /**
     * API #21 관리자 출결 수정 (패널티 차액 정산 + 공결 횟수 조정)
     */
    @Transactional
    public Attendance adminUpdateAttendance(Long attendanceId, AttendanceStatus newStatus, String reason, LocalDateTime checkedInAt) {
        Attendance attendance = attendanceRepository.findByIdForUpdate(attendanceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ATTENDANCE_NOT_FOUND));

        Session session = attendance.getSession();
        Long memberId = attendance.getMember().getId();

        CohortMember cohortMember = cohortMemberRepository.findByCohortIdAndMemberIdForUpdate(session.getCohort().getId(), memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        AttendanceStatus oldStatus = attendance.getStatus();
        int oldPenalty = attendance.getPenaltyAmount();

        // 공결 횟수 조정 (요구사항)
        adjustExcuseCount(cohortMember, oldStatus, newStatus);

        int newLateMinutes = 0;
        if (newStatus == AttendanceStatus.LATE) {
            LocalDateTime base = checkedInAt == null ? attendance.getCheckedInAt() : checkedInAt;
            newLateMinutes = attendanceJudge.calculateLateMinutes(session, base);
        }
        int newPenalty = penaltyCalculator.calculate(newStatus, newLateMinutes);

        int delta = newPenalty - oldPenalty;
        if (delta > 0) {
            applyPenalty(cohortMember, attendance, delta, "출결 수정 패널티 추가 차감");
        } else if (delta < 0) {
            applyRefund(cohortMember, attendance, -delta, "출결 수정 패널티 환급");
        }

        attendance.updateByAdmin(newStatus, newLateMinutes, newPenalty, reason);
        return attendance;
    }

    private void adjustExcuseCount(CohortMember cohortMember, AttendanceStatus oldStatus, AttendanceStatus newStatus) {
        if (oldStatus != AttendanceStatus.EXCUSED && newStatus == AttendanceStatus.EXCUSED) {
            try {
                cohortMember.increaseExcuseCount();
            } catch (IllegalStateException e) {
                throw new BusinessException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
            }
        } else if (oldStatus == AttendanceStatus.EXCUSED && newStatus != AttendanceStatus.EXCUSED) {
            cohortMember.decreaseExcuseCount();
        }
    }

    private void applyPenalty(CohortMember cohortMember, Attendance attendance, int amount, String description) {
        try {
            cohortMember.deductDeposit(amount);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.DEPOSIT_INSUFFICIENT);
        }
        depositHistoryRepository.save(
                DepositHistory.penalty(cohortMember, attendance, amount, cohortMember.getDeposit(), description)
        );
    }

    private void applyRefund(CohortMember cohortMember, Attendance attendance, int amount, String description) {
        cohortMember.refundDeposit(amount);
        depositHistoryRepository.save(
                DepositHistory.refund(cohortMember, attendance, amount, cohortMember.getDeposit(), description)
        );
    }
}