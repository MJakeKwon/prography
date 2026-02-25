package com.prography.backend.domain.deposit.entity;

import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.cohort.entity.CohortMember;
import com.prography.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "deposit_histories",
        indexes = {
                @Index(name = "idx_deposit_histories_cohort_member_id", columnList = "cohort_member_id"),
                @Index(name = "idx_deposit_histories_attendance_id", columnList = "attendance_id"),
                @Index(name = "idx_deposit_histories_type", columnList = "type")
        }
)
public class DepositHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_deposit_histories_cohort_member"))
    private CohortMember cohortMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id",
            foreignKey = @ForeignKey(name = "fk_deposit_histories_attendance"))
    private Attendance attendance;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DepositType type;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Builder
    public DepositHistory(CohortMember cohortMember, Attendance attendance, DepositType type,
                          Integer amount, Integer balanceAfter, String description) {
        this.cohortMember = cohortMember;
        this.attendance = attendance;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    public static DepositHistory initial(CohortMember cohortMember, int amount, int balanceAfter, String description) {
        return DepositHistory.builder()
                .cohortMember(cohortMember)
                .attendance(null)
                .type(DepositType.INITIAL)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();
    }

    public static DepositHistory penalty(CohortMember cohortMember, Attendance attendance, int amount, int balanceAfter, String description) {
        return DepositHistory.builder()
                .cohortMember(cohortMember)
                .attendance(attendance)
                .type(DepositType.PENALTY)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();
    }

    public static DepositHistory refund(CohortMember cohortMember, Attendance attendance, int amount, int balanceAfter, String description) {
        return DepositHistory.builder()
                .cohortMember(cohortMember)
                .attendance(attendance)
                .type(DepositType.REFUND)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();
    }
}