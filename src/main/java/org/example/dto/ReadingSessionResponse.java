
package org.example.dto;

import org.example.model.ReadingSession;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record ReadingSessionResponse(
        UUID id,
        String bibleId,
        Instant startedAt,
        Instant endedAt,
        Long durationSeconds,
        boolean active,
        Instant createdAt
) {

    public static ReadingSessionResponse from(ReadingSession session) {
        Instant startedAt = session.getStartedAt();
        Instant endedAt = session.getEndedAt();

        Long durationSeconds = endedAt == null
                ? null
                : Math.max(0L, Duration.between(startedAt, endedAt).getSeconds());

        return new ReadingSessionResponse(
                session.getId(),
                session.getBibleId(),
                startedAt,
                endedAt,
                durationSeconds,
                endedAt == null,
                session.getCreatedAt()
        );
    }
}
