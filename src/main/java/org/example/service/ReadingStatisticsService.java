package org.example.service;

import org.example.dto.ReadingActivityByHourResponse;
import org.example.dto.ReadingStatisticsResponse;
import org.example.model.AppUser;
import org.example.model.ReadingSession;
import org.example.repository.AppUserRepository;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReadingStatisticsService {

    private final ReadingSessionRepository readingSessionRepository;
    private final AppUserRepository appUserRepository;

    public ReadingStatisticsService(
            ReadingSessionRepository readingSessionRepository,
            AppUserRepository appUserRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
        this.appUserRepository = appUserRepository;
    }

    // -------------------------------------------------------------------------
    // Convenience methods — period boundaries derived from the user's timezone
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReadingStatisticsResponse getDailyStatistics(UUID userId) {
        ZoneId zone = resolveZone(userId);
        LocalDate today = LocalDate.now(zone);
        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        return getStatistics(userId, start, end);
    }

    @Transactional(readOnly = true)
    public ReadingStatisticsResponse getWeeklyStatistics(UUID userId) {
        ZoneId zone = resolveZone(userId);
        LocalDate today = LocalDate.now(zone);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Instant start = weekStart.atStartOfDay(zone).toInstant();
        Instant end = weekStart.plusWeeks(1).atStartOfDay(zone).toInstant();
        return getStatistics(userId, start, end);
    }

    @Transactional(readOnly = true)
    public ReadingStatisticsResponse getMonthlyStatistics(UUID userId) {
        ZoneId zone = resolveZone(userId);
        LocalDate today = LocalDate.now(zone);
        LocalDate monthStart = today.withDayOfMonth(1);
        Instant start = monthStart.atStartOfDay(zone).toInstant();
        Instant end = monthStart.plusMonths(1).atStartOfDay(zone).toInstant();
        return getStatistics(userId, start, end);
    }

    @Transactional(readOnly = true)
    public List<ReadingActivityByHourResponse> getWeeklyActivityByHour(UUID userId) {
        ZoneId zone = resolveZone(userId);
        LocalDate today = LocalDate.now(zone);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Instant start = weekStart.atStartOfDay(zone).toInstant();
        Instant end = weekStart.plusWeeks(1).atStartOfDay(zone).toInstant();
        return getActivityByHour(userId, start, end, zone);
    }

    // -------------------------------------------------------------------------
    // Core calculation methods (accept explicit period bounds)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReadingStatisticsResponse getStatistics(
            UUID userId,
            Instant periodStart,
            Instant periodEnd
    ) {
        validatePeriod(periodStart, periodEnd);

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(
                        userId, periodStart, periodEnd);

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

                // Count a completed session whose end falls inside the period.
                if (!sessionEnd.isBefore(periodStart) && sessionEnd.isBefore(periodEnd)) {
                    completedSessions++;
                    totalCompletedSessionSeconds +=
                            Duration.between(sessionStart, sessionEnd).getSeconds();
                }
            }

            if (effectiveEnd.isAfter(effectiveStart)) {
                totalReadingSeconds +=
                        Duration.between(effectiveStart, effectiveEnd).getSeconds();
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
        ZoneId zone = resolveZone(userId);
        return getActivityByHour(userId, periodStart, periodEnd, zone);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<ReadingActivityByHourResponse> getActivityByHour(
            UUID userId,
            Instant periodStart,
            Instant periodEnd,
            ZoneId zone
    ) {
        validatePeriod(periodStart, periodEnd);

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(
                        userId, periodStart, periodEnd);

        Instant now = Instant.now();

        long[] sessionCounts = new long[24];
        long[] readingSeconds = new long[24];

        for (ReadingSession session : sessions) {
            Instant sessionStart = session.getStartedAt();
            Instant sessionEnd = session.getEndedAt();

            // Attribute a session to the local hour it started in.
            if (!sessionStart.isBefore(periodStart) && sessionStart.isBefore(periodEnd)) {
                int startHour = sessionStart.atZone(zone).getHour();
                sessionCounts[startHour]++;
            }

            Instant effectiveStart = sessionStart.isAfter(periodStart)
                    ? sessionStart
                    : periodStart;

            Instant effectiveEnd;

            if (sessionEnd == null) {
                effectiveEnd = now.isBefore(periodEnd) ? now : periodEnd;
            } else {
                effectiveEnd = sessionEnd.isBefore(periodEnd) ? sessionEnd : periodEnd;
            }

            if (!effectiveEnd.isAfter(effectiveStart)) {
                continue;
            }

            // Distribute the duration across each local hour it overlaps.
            Instant cursor = effectiveStart;

            while (cursor.isBefore(effectiveEnd)) {
                // Snap cursor back to the start of its local hour.
                Instant hourStart = cursor.atZone(zone)
                        .withMinute(0).withSecond(0).withNano(0)
                        .toInstant();

                Instant nextHour = hourStart.plus(Duration.ofHours(1));

                Instant segmentEnd = effectiveEnd.isBefore(nextHour)
                        ? effectiveEnd
                        : nextHour;

                int hour = cursor.atZone(zone).getHour();
                readingSeconds[hour] +=
                        Duration.between(cursor, segmentEnd).getSeconds();

                cursor = segmentEnd;
            }
        }

        List<ReadingActivityByHourResponse> results = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            results.add(new ReadingActivityByHourResponse(
                    hour, sessionCounts[hour], readingSeconds[hour]));
        }
        return results;
    }

    /**
     * Resolves the user's stored IANA timezone, falling back to UTC if the
     * record is missing or the stored value is somehow invalid.
     */
    private ZoneId resolveZone(UUID userId) {
        return appUserRepository.findById(userId)
                .map(AppUser::getTimezone)
                .map(tz -> {
                    try {
                        return ZoneId.of(tz);
                    } catch (Exception e) {
                        return ZoneId.of("UTC");
                    }
                })
                .orElse(ZoneId.of("UTC"));
    }

    private void validatePeriod(Instant periodStart, Instant periodEnd) {
        if (periodStart == null || periodEnd == null
                || !periodStart.isBefore(periodEnd)) {
            throw new IllegalArgumentException(
                    "The period start must be before the period end.");
        }
    }
}
