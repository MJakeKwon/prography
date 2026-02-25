package com.prography.backend.domain.attendance.entity;

import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.session.entity.QRCode;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendances_session_id_member_id", columnNames = {"session_id", "member_id"})
        },
        indexes = {
                @Index(name = "idx_attendances_member_id", columnList = "member_id"),
                @Index(name = "idx_attendances_qrcode_id", columnList = "qrcode_id"),
                @Index(name = "idx_attendances_status", columnList = "status")
        }
)
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendances_session"))
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_attendances_member"))
    private Member member;

    // 관리자 수기 등록 고려 -> nullable 허용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qrcode_id",
            foreignKey = @ForeignKey(name = "fk_attendances_qrcode"))
    private QRCode qrcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes;

    @Column(name = "penalty_amount", nullable = false)
    private Integer penaltyAmount;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;

    @Builder
    public Attendance(Session session, Member member, QRCode qrcode,
                      AttendanceStatus status, Integer lateMinutes, Integer penaltyAmount,
                      String reason, LocalDateTime checkedInAt) {
        this.session = session;
        this.member = member;
        this.qrcode = qrcode;
        this.status = status;
        this.lateMinutes = lateMinutes == null ? 0 : lateMinutes;
        this.penaltyAmount = penaltyAmount == null ? 0 : penaltyAmount;
        this.reason = reason;
        this.checkedInAt = checkedInAt;
    }

    public void updateByAdmin(AttendanceStatus status, int lateMinutes, int penaltyAmount, String reason) {
        this.status = status;
        this.lateMinutes = lateMinutes;
        this.penaltyAmount = penaltyAmount;
        this.reason = reason;
    }

    public boolean isExcused() {
        return this.status == AttendanceStatus.EXCUSED;
    }
}