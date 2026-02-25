package com.prography.backend.domain.session.service;

import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.domain.cohort.service.CohortReadService;
import com.prography.backend.domain.session.entity.QRCode;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.entity.SessionStatus;
import com.prography.backend.domain.session.repository.QRCodeRepository;
import com.prography.backend.domain.session.repository.SessionRepository;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionAdminService {

    private final SessionRepository sessionRepository;
    private final QRCodeRepository qrCodeRepository;
    private final CohortReadService cohortReadService;

    @Transactional(readOnly = true)
    public List<Session> getMemberSessions() {
        Cohort current = cohortReadService.getCurrentCohort();
        return sessionRepository.findAllByCohortIdAndStatusNotOrderBySessionDateAscSessionTimeAsc(
                current.getId(), SessionStatus.CANCELLED
        );
    }

    @Transactional(readOnly = true)
    public List<Session> getAdminSessions() {
        Cohort current = cohortReadService.getCurrentCohort();
        return sessionRepository.findAllByCohortIdOrderBySessionDateAscSessionTimeAsc(current.getId());
    }

    @Transactional
    public Session createSession(
            String title,
            LocalDate sessionDate,
            LocalTime sessionTime,
            String location,
            SessionStatus status // null이면 SCHEDULED
    ) {
        Cohort current = cohortReadService.getCurrentCohort();

        Session session = Session.builder()
                .cohort(current)
                .title(title)
                .sessionDate(sessionDate)
                .sessionTime(sessionTime)
                .location(location)
                .status(status == null ? SessionStatus.SCHEDULED : status)
                .build();
        sessionRepository.save(session);

        // 일정 생성 시 QR 자동 생성
        qrCodeRepository.save(newQRCode(session, LocalDateTime.now()));

        return session;
    }

    @Transactional
    public Session updateSession(Long sessionId, String title, LocalDate sessionDate, LocalTime sessionTime, String location) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (session.isCancelled()) {
            throw new BusinessException(ErrorCode.SESSION_CANCELLED_CANNOT_UPDATE);
        }

        session.update(title, sessionDate, sessionTime, location);
        return session;
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        session.cancel(); // soft delete
    }

    @Transactional
    public QRCode createQRCode(Long sessionId) {
        Session session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        // 기존 활성 QR 있으면 즉시 폐기 (정책상 활성 1개)
        qrCodeRepository.findLatestActiveBySessionIdForUpdate(sessionId, now)
                .ifPresent(qr -> qr.revoke(now));

        QRCode newQr = newQRCode(session, now);
        return qrCodeRepository.save(newQr);
    }

    @Transactional
    public QRCode refreshQRCode(Long qrCodeId) {
        QRCode target = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QR_INVALID));

        Session session = sessionRepository.findByIdForUpdate(target.getSession().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        qrCodeRepository.findLatestActiveBySessionIdForUpdate(session.getId(), now)
                .ifPresent(active -> active.revoke(now));

        QRCode newQr = newQRCode(session, now);
        return qrCodeRepository.save(newQr);
    }

    private QRCode newQRCode(Session session, LocalDateTime now) {
        return QRCode.builder()
                .session(session)
                .hashValue(UUID.randomUUID().toString())
                .expiresAt(now.plusHours(24))
                .revokedAt(null)
                .build();
    }
}