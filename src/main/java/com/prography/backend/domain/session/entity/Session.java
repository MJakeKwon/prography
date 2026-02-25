package com.prography.backend.domain.session.entity;

import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "idx_sessions_cohort_id", columnList = "cohort_id"),
                @Index(name = "idx_sessions_session_date", columnList = "session_date"),
                @Index(name = "idx_sessions_status", columnList = "status")
        }
)
public class Session extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessions_cohort"))
    private Cohort cohort;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_time", nullable = false)
    private LocalTime sessionTime;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Builder
    public Session(Cohort cohort, String title, LocalDate sessionDate, LocalTime sessionTime, String location, SessionStatus status) {
        this.cohort = cohort;
        this.title = title;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.location = location;
        this.status = status;
    }

    public void update(String title, LocalDate sessionDate, LocalTime sessionTime, String location) {
        if (this.status == SessionStatus.CANCELLED) {
            throw new IllegalStateException("CANCELLED_SESSION_CANNOT_BE_UPDATED");
        }
        this.title = title;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.location = location;
    }

    public void cancel() {
        this.status = SessionStatus.CANCELLED;
    }
    public boolean isCancelled() {
        return this.status == SessionStatus.CANCELLED;
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(sessionDate, sessionTime);
    }

    public boolean isInProgress() {
        return this.status == SessionStatus.IN_PROGRESS;
    }
}