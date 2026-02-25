package com.prography.backend.domain.cohort.entity;

import com.prography.backend.domain.member.entity.Member;
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
        name = "cohort_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cohort_members_cohort_id_member_id", columnNames = {"cohort_id", "member_id"})
        },
        indexes = {
                @Index(name = "idx_cohort_members_member_id", columnList = "member_id"),
                @Index(name = "idx_cohort_members_part_id", columnList = "part_id"),
                @Index(name = "idx_cohort_members_team_id", columnList = "team_id")
        }
)
public class CohortMember extends BaseTimeEntity {

    public static final int INITIAL_DEPOSIT = 100_000;
    public static final int MAX_EXCUSE_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 추후 낙관적 락 쓰고 싶으면 추가
    // @Version
    // private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cohort_members_cohort"))
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cohort_members_member"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cohort_members_part"))
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",
            foreignKey = @ForeignKey(name = "fk_cohort_members_team"))
    private Team team;

    @Column(name = "deposit", nullable = false)
    private Integer deposit;

    @Column(name = "excuse_count", nullable = false)
    private Integer excuseCount;

    @Builder
    public CohortMember(Cohort cohort, Member member, Part part, Team team, Integer deposit, Integer excuseCount) {
        this.cohort = cohort;
        this.member = member;
        this.part = part;
        this.team = team;
        this.deposit = (deposit == null) ? INITIAL_DEPOSIT : deposit;
        this.excuseCount = (excuseCount == null) ? 0 : excuseCount;
    }

    public void updateAssignment(Part part, Team team) {
        this.part = part;
        this.team = team;
    }

    public void deductDeposit(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (this.deposit < amount) throw new IllegalStateException("DEPOSIT_INSUFFICIENT");
        this.deposit -= amount;
    }

    public void refundDeposit(int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0");
        this.deposit += amount;
    }

    public void increaseExcuseCount() {
        if (this.excuseCount >= MAX_EXCUSE_COUNT) {
            throw new IllegalStateException("EXCUSE_LIMIT_EXCEEDED");
        }
        this.excuseCount += 1;
    }

    public void decreaseExcuseCount() {
        if (this.excuseCount > 0) this.excuseCount -= 1;
    }
}