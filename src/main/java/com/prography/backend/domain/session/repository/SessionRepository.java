package com.prography.backend.domain.session.repository;

import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.entity.SessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findAllByCohortIdOrderBySessionDateAscSessionTimeAsc(Long cohortId);

    List<Session> findAllByCohortIdAndStatusNotOrderBySessionDateAscSessionTimeAsc(Long cohortId, SessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.id = :id")
    Optional<Session> findByIdForUpdate(@Param("id") Long id);
}