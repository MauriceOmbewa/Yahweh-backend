
package org.example.service;

import org.example.dto.ReadingStatisticsResponse;
import org.example.model.ReadingSession;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.dto.ReadingActivityByHourResponse;

import java.time.ZoneOffset;
import java.util.ArrayList;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReadingStatisticsService {

    private final ReadingSessionRepository readingSessionRepository;

    public ReadingStatisticsService(
            ReadingSessionRepository readingSessionRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
    }

    @Transactional(readOnly = true)
    public ReadingStatisticsResponse getStatistics(
            UUID userId,
            Instant periodStart,
            Instant periodEnd
    ) {
        if (periodStart == null || periodEnd == null
                || !periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "The period start must be before the period end."
            );
        }

        List<ReadingSession> sessions =
                readingSessionRepository
                        .findAllByUser_IdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                                userId,
                                periodStart,
                                periodEnd
                        );

        long totalReadingSeconds = 0;
        long completedSessions = 0;
        long activeSessions = 0;

        for (ReadingSession session : sessions) {
            Instant endedAt = session.getEndedAt();

            if (endedAt == null) {
                activeSessions++;
                continue;
            }

            completedSessions++;

            long durationSeconds = Math.max(
                    0L,
                    Duration.between(
                            session.getStartedAt(),
                            endedAt
                    ).getSeconds()
            );

            totalReadingSeconds += durationSeconds;
        }

        long averageSessionSeconds = completedSessions == 0
                ? 0
                : totalReadingSeconds / completedSessions;

        return new ReadingStatisticsResponse(
                periodStart,
                periodEnd,
                totalReadingSeconds,
                sessions.size(),
                averageSessionSeconds,
                completedSessions,
                activeSessions
        );
    }

    @Transactional(readOnly = true)
    public List<ReadingActivityByHourResponse> getActivityByHour(
            UUID userId,
            Instant periodStart,
            Instant periodEnd
    ) {
        if (periodStart == null || periodEnd == null
                || !periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "The period start must be before the period end."
            );
        }

        List<ReadingSession> sessions =
                readingSessionRepository
                        .findAllByUser_IdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                                userId,
                                periodStart,
                                periodEnd
                        );

        long[] sessionCounts = new long[24];
        long[] readingSeconds = new long[24];

        for (ReadingSession session : sessions) {
            int hour = session.getStartedAt()
                    .atZone(ZoneOffset.UTC)
                    .getHour();

            sessionCounts[hour]++;

            if (session.getEndedAt() != null) {
                long duration = Math.max(
                        0L,
                        Duration.between(
                                session.getStartedAt(),
                                session.getEndedAt()
                        ).getSeconds()
                );

                readingSeconds[hour] += duration;
            }
        }

        List<ReadingActivityByHourResponse> results = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            results.add(new ReadingActivityByHourResponse(
                    hour,
                    sessionCounts[hour],
                    readingSeconds[hour]
            ));
        }

        return results;
    }
}
