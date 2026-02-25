package com.prography.backend.domain.cohort.repository;

import com.prography.backend.domain.cohort.entity.CohortMember;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CohortMemberRepository extends JpaRepository<CohortMember, Long> {

    Optional<CohortMember> findByCohortIdAndMemberId(Long cohortId, Long memberId);

    boolean existsByCohortIdAndMemberId(Long cohortId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cm from CohortMember cm where cm.cohort.id = :cohortId and cm.member.id = :memberId")
    Optional<CohortMember> findByCohortIdAndMemberIdForUpdate(@Param("cohortId") Long cohortId,
                                                              @Param("memberId") Long memberId);
}