
package org.example.service;

import org.example.dto.ReadingStreakResponse;
import org.example.dto.ReadingDayResponse;
import org.example.model.ReadingSession;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class ReadingStreakService {

    private final ReadingSessionRepository readingSessionRepository;

    public ReadingStreakService(
            ReadingSessionRepository readingSessionRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
    }

    @Transactional(readOnly = true)
    public ReadingStreakResponse getStreak(UUID userId) {
        List<ReadingSession> sessions =
                readingSessionRepository
                        .findAllByUser_IdOrderByStartedAtDesc(userId);

        NavigableSet<LocalDate> readingDates = new TreeSet<>();

        for (ReadingSession session : sessions) {
            // Only completed sessions count toward a reading streak.
            if (session.getEndedAt() == null) {
                continue;
            }

            LocalDate readingDate = session.getStartedAt()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            readingDates.add(readingDate);
        }

        if (readingDates.isEmpty()) {
            return new ReadingStreakResponse(
                    0,
                    0,
                    0,
                    null
            );
        }

        LocalDate lastReadingDate = readingDates.last();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        int currentStreak = 0;

        // A streak remains current if the last reading day was today
        // or yesterday. Otherwise, the current streak is zero.
        if (!lastReadingDate.isBefore(today.minusDays(1))) {
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
            if (previousDate != null
                    && date.equals(previousDate.plusDays(1))) {
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
        if (startDate == null || endDate == null
                || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "The start date must be on or before the end date."
            );
        }

        Instant start = startDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant end = endDate
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        List<ReadingSession> sessions =
                readingSessionRepository
                        .findAllByUser_IdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                                userId,
                                start,
                                end
                        );

        Map<LocalDate, Long> completedSessionsByDate = new HashMap<>();

        for (ReadingSession session : sessions) {
            if (session.getEndedAt() == null) {
                continue;
            }

            LocalDate date = session.getStartedAt()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            completedSessionsByDate.merge(date, 1L, Long::sum);
        }

        List<ReadingDayResponse> calendar = new ArrayList<>();

        for (LocalDate date = startDate;
             !date.isAfter(endDate);
             date = date.plusDays(1)) {

            long completedSessions =
                    completedSessionsByDate.getOrDefault(date, 0L);

            calendar.add(new ReadingDayResponse(
                    date,
                    completedSessions > 0,
                    completedSessions
            ));
        }

        return calendar;
    }

}
