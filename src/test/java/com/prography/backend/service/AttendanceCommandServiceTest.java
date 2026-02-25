package com.prography.backend.service;

import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import com.prography.backend.domain.attendance.repository.AttendanceRepository;
import com.prography.backend.domain.attendance.service.AttendanceCommandService;
import com.prography.backend.domain.attendance.service.AttendanceJudge;
import com.prography.backend.domain.attendance.service.PenaltyCalculator;
import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.domain.cohort.entity.CohortMember;
import com.prography.backend.domain.cohort.repository.CohortMemberRepository;
import com.prography.backend.domain.deposit.repository.DepositHistoryRepository;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.entity.Role;
import com.prography.backend.domain.member.repository.MemberRepository;
import com.prography.backend.domain.session.entity.QRCode;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.entity.SessionStatus;
import com.prography.backend.domain.session.repository.QRCodeRepository;
import com.prography.backend.domain.session.repository.SessionRepository;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AttendanceCommandServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private QRCodeRepository qrCodeRepository;
    @Mock private CohortMemberRepository cohortMemberRepository;
    @Mock private DepositHistoryRepository depositHistoryRepository;

    private AttendanceJudge attendanceJudge;
    private PenaltyCalculator penaltyCalculator;

    @InjectMocks
    private AttendanceCommandService attendanceCommandService;

    @BeforeEach
    void setUp() {
        attendanceJudge = spy(new AttendanceJudge());
        penaltyCalculator = spy(new PenaltyCalculator());

        attendanceCommandService = new AttendanceCommandService(
                attendanceRepository,
                memberRepository,
                sessionRepository,
                qrCodeRepository,
                cohortMemberRepository,
                depositHistoryRepository,
                attendanceJudge,
                penaltyCalculator
        );
    }

    @Test
    @DisplayName("QR 출석 체크 성공 - PRESENT 저장, 패널티 0원이면 보증금 이력 저장 안함")
    void checkInByQr_success_present() {
        // given
        Long memberId = 1L;
        String hash = "valid-qr";

        Cohort cohort = cohort(11L, 11, "11기");
        Session session = session(100L, cohort, SessionStatus.IN_PROGRESS,
                LocalDate.now(), LocalTime.now().plusMinutes(10)); // 시작 전 => PRESENT

        QRCode qrCode = qrCode(1000L, session, hash, LocalDateTime.now().plusHours(1), null);

        Member member = member(memberId, "user1", MemberStatus.ACTIVE);
        CohortMember cohortMember = cohortMember(200L, cohort, member, 100_000, 0);

        given(qrCodeRepository.findByHashValue(hash)).willReturn(Optional.of(qrCode));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(attendanceRepository.existsBySessionIdAndMemberId(session.getId(), memberId)).willReturn(false);
        given(cohortMemberRepository.findByCohortIdAndMemberIdForUpdate(cohort.getId(), memberId))
                .willReturn(Optional.of(cohortMember));

        // save 시 전달된 Attendance 그대로 반환
        given(attendanceRepository.save(any(Attendance.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Attendance result = attendanceCommandService.checkInByQr(hash, memberId);

        // then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getPenaltyAmount()).isEqualTo(0);

        verify(attendanceRepository).save(any(Attendance.class));
        verify(depositHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("QR 출석 체크 실패 - 중복 출결")
    void checkInByQr_fail_duplicate() {
        // given
        Long memberId = 1L;
        String hash = "valid-qr";

        Cohort cohort = cohort(11L, 11, "11기");
        Session session = session(100L, cohort, SessionStatus.IN_PROGRESS,
                LocalDate.now(), LocalTime.now());
        QRCode qrCode = qrCode(1000L, session, hash, LocalDateTime.now().plusHours(1), null);
        Member member = member(memberId, "user1", MemberStatus.ACTIVE);

        given(qrCodeRepository.findByHashValue(hash)).willReturn(Optional.of(qrCode));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(attendanceRepository.existsBySessionIdAndMemberId(session.getId(), memberId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> attendanceCommandService.checkInByQr(hash, memberId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ATTENDANCE_ALREADY_CHECKED);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("QR 출석 체크 실패 - 보증금 부족")
    void checkInByQr_fail_deposit_insufficient() {
        // given
        Long memberId = 1L;
        String hash = "valid-qr";

        Cohort cohort = cohort(11L, 11, "11기");
        Session session = session(100L, cohort, SessionStatus.IN_PROGRESS,
                LocalDate.now(), LocalTime.now().minusMinutes(30)); // 시작 후 => LATE (패널티 발생)
        QRCode qrCode = qrCode(1000L, session, hash, LocalDateTime.now().plusHours(1), null);

        Member member = member(memberId, "user1", MemberStatus.ACTIVE);

        // 보증금 부족 상황 만들기
        CohortMember cohortMember = cohortMember(200L, cohort, member, 0, 0);

        given(qrCodeRepository.findByHashValue(hash)).willReturn(Optional.of(qrCode));
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(attendanceRepository.existsBySessionIdAndMemberId(session.getId(), memberId)).willReturn(false);
        given(cohortMemberRepository.findByCohortIdAndMemberIdForUpdate(cohort.getId(), memberId))
                .willReturn(Optional.of(cohortMember));
        given(attendanceRepository.save(any(Attendance.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when & then
        assertThatThrownBy(() -> attendanceCommandService.checkInByQr(hash, memberId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DEPOSIT_INSUFFICIENT);
    }

    // ===== test fixture helpers =====

    private Cohort cohort(Long id, Integer generation, String name) {
        Cohort cohort = Cohort.builder()
                .generation(generation)
                .name(name)
                .build();
        setField(cohort, "id", id);
        return cohort;
    }

    private Session session(Long id, Cohort cohort, SessionStatus status, LocalDate date, LocalTime time) {
        Session session = Session.builder()
                .cohort(cohort)
                .title("세션")
                .sessionDate(date)
                .sessionTime(time)
                .location("강남")
                .status(status)
                .build();
        setField(session, "id", id);
        return session;
    }

    private QRCode qrCode(Long id, Session session, String hashValue, LocalDateTime expiresAt, LocalDateTime revokedAt) {
        QRCode qrCode = QRCode.builder()
                .session(session)
                .hashValue(hashValue)
                .expiresAt(expiresAt)
                .revokedAt(revokedAt)
                .build();
        setField(qrCode, "id", id);
        return qrCode;
    }

    private Member member(Long id, String loginId, MemberStatus status) {
        Member member = Member.builder()
                .loginId(loginId)
                .passwordHash("hashed")
                .name("테스트")
                .phone("010-1111-1111")
                .status(status)
                .role(Role.MEMBER)
                .build();
        setField(member, "id", id);
        return member;
    }

    private CohortMember cohortMember(Long id, Cohort cohort, Member member, int deposit, int excuseCount) {
        CohortMember cm = CohortMember.builder()
                .cohort(cohort)
                .member(member)
                .part(null)
                .team(null)
                .deposit(deposit)
                .excuseCount(excuseCount)
                .build();
        setField(cm, "id", id);
        return cm;
    }

    private void setField(Object target, String fieldName, Object value) {
        org.springframework.test.util.ReflectionTestUtils.setField(target, fieldName, value);
    }
}