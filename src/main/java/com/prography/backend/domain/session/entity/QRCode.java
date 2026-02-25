package com.prography.backend.domain.session.entity;

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
        name = "qrcodes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_qrcodes_hash_value", columnNames = "hash_value")
        },
        indexes = {
                @Index(name = "idx_qrcodes_session_id", columnList = "session_id"),
                @Index(name = "idx_qrcodes_expires_at", columnList = "expires_at")
        }
)
public class QRCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_qrcodes_session"))
    private Session session;

    @Column(name = "hash_value", nullable = false, length = 64)
    private String hashValue;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Builder
    public QRCode(Session session, String hashValue, LocalDateTime expiresAt, LocalDateTime revokedAt) {
        this.session = session;
        this.hashValue = hashValue;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public void revoke(LocalDateTime now) {
        this.revokedAt = now;
    }

    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now); // expiresAt <= now
    }

    public boolean isUsable(LocalDateTime now) {
        return !isRevoked() && !isExpired(now);
    }
}