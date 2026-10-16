
package org.example.service;

import org.example.dto.ReadingActivityByHourResponse;
import org.example.dto.ReadingStatisticsResponse;
import org.example.model.ReadingSession;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
        validatePeriod(periodStart, periodEnd);

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(
                        userId,
                        periodStart,
                        periodEnd
                );

        Instant now = Instant.now();

        long totalReadingSeconds = 0;
        long totalCompletedSessionSeconds = 0;
        long completedSessions = 0;
        long activeSessions = 0;

        for (ReadingSession session : sessions) {
            Instant sessionStart = session.getStartedAt();
            Instant sessionEnd = session.getEndedAt();

            Instant effectiveStart = sessionStart.isAfter(periodStart)
                    ? sessionStart
                    : periodStart;

            Instant effectiveEnd;

            if (sessionEnd == null) {
                activeSessions++;
                effectiveEnd = now.isBefore(periodEnd) ? now : periodEnd;
            } else {
                effectiveEnd = sessionEnd.isBefore(periodEnd)
                        ? sessionEnd
                        : periodEnd;

                // Count completed sessions whose end time falls in this period.
                if (!sessionEnd.isBefore(periodStart)
                        && sessionEnd.isBefore(periodEnd)) {
                    completedSessions++;
                    totalCompletedSessionSeconds += Duration.between(
                            sessionStart,
                            sessionEnd
                    ).getSeconds();
                }
            }

            if (effectiveEnd.isAfter(effectiveStart)) {
                long durationSeconds = Duration.between(
                        effectiveStart,
                        effectiveEnd
                ).getSeconds();

                totalReadingSeconds += durationSeconds;
            }
        }

        long averageSessionSeconds = completedSessions == 0
                ? 0
                : totalCompletedSessionSeconds / completedSessions;

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
        validatePeriod(periodStart, periodEnd);

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(
                        userId,
                        periodStart,
                        periodEnd
                );

        Instant now = Instant.now();

        long[] sessionCounts = new long[24];
        long[] readingSeconds = new long[24];

        for (ReadingSession session : sessions) {
            Instant sessionStart = session.getStartedAt();
            Instant sessionEnd = session.getEndedAt();

            // Count a session in the hour when it actually started,
            // provided its start is inside the requested period.
            if (!sessionStart.isBefore(periodStart)
                    && sessionStart.isBefore(periodEnd)) {
                int startHour = sessionStart
                        .atZone(ZoneOffset.UTC)
                        .getHour();

                sessionCounts[startHour]++;
            }

            Instant effectiveStart = sessionStart.isAfter(periodStart)
                    ? sessionStart
                    : periodStart;

            Instant effectiveEnd;

            if (sessionEnd == null) {
                effectiveEnd = now.isBefore(periodEnd) ? now : periodEnd;
            } else {
                effectiveEnd = sessionEnd.isBefore(periodEnd)
                        ? sessionEnd
                        : periodEnd;
            }

            if (!effectiveEnd.isAfter(effectiveStart)) {
                continue;
            }

            // Distribute the duration across each UTC hour it overlaps.
            Instant cursor = effectiveStart;

            while (cursor.isBefore(effectiveEnd)) {
                Instant hourStart = cursor
                        .atZone(ZoneOffset.UTC)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .toInstant();

                Instant nextHour = hourStart.plus(Duration.ofHours(1));

                Instant segmentEnd = effectiveEnd.isBefore(nextHour)
                        ? effectiveEnd
                        : nextHour;

                int hour = cursor.atZone(ZoneOffset.UTC).getHour();

                readingSeconds[hour] += Duration.between(
                        cursor,
                        segmentEnd
                ).getSeconds();

                cursor = segmentEnd;
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

    private void validatePeriod(
            Instant periodStart,
            Instant periodEnd
    ) {
        if (periodStart == null || periodEnd == null
                || !periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "The period start must be before the period end."
            );
        }
    }
}
