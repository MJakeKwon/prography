package com.prography.backend.domain.attendance.repository;

import com.prography.backend.domain.attendance.entity.Attendance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    boolean existsBySessionIdAndMemberId(Long sessionId, Long memberId);

    Optional<Attendance> findBySessionIdAndMemberId(Long sessionId, Long memberId);

    List<Attendance> findAllByMemberIdOrderByIdDesc(Long memberId);

    List<Attendance> findAllBySessionIdOrderByIdAsc(Long sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Attendance a where a.id = :id")
    Optional<Attendance> findByIdForUpdate(@Param("id") Long id);
}