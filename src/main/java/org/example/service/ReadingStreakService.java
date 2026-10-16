
package org.example.service;

import org.example.dto.ReadingDayResponse;
import org.example.dto.ReadingStreakResponse;
import org.example.model.ReadingSession;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
            if (session.getEndedAt() == null) {
                continue;
            }

            addReadingDates(
                    readingDates,
                    session.getStartedAt(),
                    session.getEndedAt()
            );
        }

        if (readingDates.isEmpty()) {
            return new ReadingStreakResponse(0, 0, 0, null);
        }

        LocalDate lastReadingDate = readingDates.last();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        int currentStreak = 0;

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

        Instant start = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = endDate.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(
                        userId,
                        start,
                        end
                );

        Map<LocalDate, Long> completedSessionsByDate = new HashMap<>();

        for (ReadingSession session : sessions) {
            Instant sessionEnd = session.getEndedAt();

            if (sessionEnd == null
                    || !sessionEnd.isAfter(session.getStartedAt())) {
                continue;
            }

            LocalDate firstDate = session.getStartedAt()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            // The session end is exclusive. If it ends exactly at
            // midnight, that next day does not receive a session count.
            LocalDate lastDate = sessionEnd.minusNanos(1)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            LocalDate firstIncluded = firstDate.isBefore(startDate)
                    ? startDate
                    : firstDate;

            LocalDate lastIncluded = lastDate.isAfter(endDate)
                    ? endDate
                    : lastDate;

            for (LocalDate date = firstIncluded;
                 !date.isAfter(lastIncluded);
                 date = date.plusDays(1)) {

                completedSessionsByDate.merge(date, 1L, Long::sum);
            }
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

    private void addReadingDates(
            NavigableSet<LocalDate> readingDates,
            Instant startedAt,
            Instant endedAt
    ) {
        if (startedAt == null || endedAt == null
                || !endedAt.isAfter(startedAt)) {
            return;
        }

        LocalDate firstDate = startedAt
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        LocalDate lastDate = endedAt.minusNanos(1)
                .atZone(ZoneOffset.UTC)
                .toLocalDate();

        for (LocalDate date = firstDate;
             !date.isAfter(lastDate);
             date = date.plusDays(1)) {
            readingDates.add(date);
        }
    }
}
