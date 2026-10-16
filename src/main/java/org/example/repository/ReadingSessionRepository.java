
package org.example.repository;

import org.example.model.ReadingSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingSessionRepository
        extends JpaRepository<ReadingSession, UUID> {

    List<ReadingSession> findAllByUser_IdOrderByStartedAtDesc(UUID userId);

    Page<ReadingSession> findAllByUser_IdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    Optional<ReadingSession> findByIdAndUser_Id(
            UUID sessionId,
            UUID userId
    );

    Optional<ReadingSession> findFirstByUser_IdAndEndedAtIsNullOrderByStartedAtDesc(
            UUID userId
    );

    List<ReadingSession> findAllByUser_IdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
            UUID userId,
            Instant start,
            Instant end
    );

    @Query("""
    SELECT s FROM ReadingSession s
    WHERE s.user.id = :userId
      AND s.startedAt < :periodEnd
      AND (s.endedAt IS NULL OR s.endedAt > :periodStart)
    ORDER BY s.startedAt DESC
    """)
    List<ReadingSession> findSessionsOverlappingPeriod(
            @Param("userId") UUID userId,
            @Param("periodStart") Instant periodStart,
            @Param("periodEnd") Instant periodEnd
    );

    long countByUser_Id(UUID userId);
}
