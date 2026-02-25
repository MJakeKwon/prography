package com.prography.backend.domain.session.repository;

import com.prography.backend.domain.session.entity.QRCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QRCodeRepository extends JpaRepository<QRCode, Long> {

    Optional<QRCode> findByHashValue(String hashValue);

    boolean existsByHashValue(String hashValue);

    @Query("""
           select q
           from QRCode q
           where q.session.id = :sessionId
             and q.revokedAt is null
             and q.expiresAt > :now
           order by q.id desc
           """)
    Optional<QRCode> findLatestActiveBySessionId(@Param("sessionId") Long sessionId,
                                                 @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select q
           from QRCode q
           where q.session.id = :sessionId
             and q.revokedAt is null
             and q.expiresAt > :now
           order by q.id desc
           """)
    Optional<QRCode> findLatestActiveBySessionIdForUpdate(@Param("sessionId") Long sessionId,
                                                          @Param("now") LocalDateTime now);
}