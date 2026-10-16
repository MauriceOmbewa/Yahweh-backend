package org.example.service;

import org.example.dto.ReadingDayResponse;
import org.example.dto.ReadingStreakResponse;
import org.example.model.AppUser;
import org.example.model.ReadingSession;
import org.example.repository.AppUserRepository;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class ReadingStreakService {

    private final ReadingSessionRepository readingSessionRepository;
    private final AppUserRepository appUserRepository;

    public ReadingStreakService(
            ReadingSessionRepository readingSessionRepository,
            AppUserRepository appUserRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public ReadingStreakResponse getStreak(UUID userId) {
        ZoneId zone = resolveZone(userId);

        List<ReadingSession> sessions =
                readingSessionRepository
                        .findAllByUser_IdOrderByStartedAtDesc(userId);

        NavigableSet<LocalDate> readingDates = new TreeSet<>();

        for (ReadingSession session : sessions) {
            if (session.getEndedAt() == null) {
                continue;
            }
            addReadingDates(readingDates, session.getStartedAt(), session.getEndedAt(), zone);
        }

        if (readingDates.isEmpty()) {
            return new ReadingStreakResponse(0, 0, 0, null);
        }

        LocalDate lastReadingDate = readingDates.last();
        LocalDate today = LocalDate.now(zone);

        int currentStreak = 0;

        // Streak is still alive if the user read today OR yesterday.
        if (!lastReadingDate.isBefore(today.minusDays(1))
                && !lastReadingDate.isAfter(today)) {

            LocalDate expectedDate = lastReadingDate;

            for (LocalDate date : readingDates.descendingSet()) {
                if (!date.equals(expectedDate)) {
                    break;
                }
                currentStreak++;
                expectedDate = expectedDate.minusDays(1);
            }
        }

        int longestStreak = 0;
        int consecutiveDays = 0;
        LocalDate previousDate = null;

        for (LocalDate date : readingDates) {
            if (previousDate != null && date.equals(previousDate.plusDays(1))) {
                consecutiveDays++;
            } else {
                consecutiveDays = 1;
            }
            longestStreak = Math.max(longestStreak, consecutiveDays);
            previousDate = date;
        }

        return new ReadingStreakResponse(
                currentStreak,
                longestStreak,
                readingDates.size(),
                lastReadingDate
        );
    }

    @Transactional(readOnly = true)
    public List<ReadingDayResponse> getReadingCalendar(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "The start date must be on or before the end date.");
        }

        ZoneId zone = resolveZone(userId);

        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(zone).toInstant();

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(userId, start, end);

        Map<LocalDate, Long> completedSessionsByDate = new HashMap<>();

        for (ReadingSession session : sessions) {
            Instant sessionEnd = session.getEndedAt();

            if (sessionEnd == null || !sessionEnd.isAfter(session.getStartedAt())) {
                continue;
            }

            LocalDate firstDate = session.getStartedAt().atZone(zone).toLocalDate();

            // Subtract one nanosecond so a session ending exactly at midnight
            // doesn't bleed into the next day.
            LocalDate lastDate = sessionEnd.minusNanos(1).atZone(zone).toLocalDate();

            LocalDate firstIncluded = firstDate.isBefore(startDate) ? startDate : firstDate;
            LocalDate lastIncluded = lastDate.isAfter(endDate) ? endDate : lastDate;

            for (LocalDate date = firstIncluded;
                 !date.isAfter(lastIncluded);
                 date = date.plusDays(1)) {
                completedSessionsByDate.merge(date, 1L, Long::sum);
            }
        }

        List<ReadingDayResponse> calendar = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            long count = completedSessionsByDate.getOrDefault(date, 0L);
            calendar.add(new ReadingDayResponse(date, count > 0, count));
        }

        return calendar;
    }

    // --- helpers ---

    private void addReadingDates(
            NavigableSet<LocalDate> readingDates,
            Instant startedAt,
            Instant endedAt,
            ZoneId zone
    ) {
        if (startedAt == null || endedAt == null || !endedAt.isAfter(startedAt)) {
            return;
        }

        LocalDate firstDate = startedAt.atZone(zone).toLocalDate();
        LocalDate lastDate = endedAt.minusNanos(1).atZone(zone).toLocalDate();

        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            readingDates.add(date);
        }
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
}
