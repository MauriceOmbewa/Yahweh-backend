
package org.example.repository;

import org.example.model.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingSessionRepository
        extends JpaRepository<ReadingSession, UUID> {

    List<ReadingSession> findAllByUser_IdOrderByStartedAtDesc(UUID userId);

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

    long countByUser_Id(UUID userId);
}
