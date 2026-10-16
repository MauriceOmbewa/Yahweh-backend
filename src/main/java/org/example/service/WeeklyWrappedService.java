package org.example.service;

import org.example.model.AppUser;
import org.example.model.ReadingProgress;
import org.example.model.ReadingSession;
import org.example.model.WeeklyWrapped;
import org.example.repository.AppUserRepository;
import org.example.repository.ReadingProgressRepository;
import org.example.repository.ReadingSessionRepository;
import org.example.repository.WeeklyWrappedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class WeeklyWrappedService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyWrappedService.class);

    private final WeeklyWrappedRepository wrappedRepository;
    private final AppUserRepository appUserRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final ReadingProgressRepository readingProgressRepository;

    public WeeklyWrappedService(
            WeeklyWrappedRepository wrappedRepository,
            AppUserRepository appUserRepository,
            ReadingSessionRepository readingSessionRepository,
            ReadingProgressRepository readingProgressRepository
    ) {
        this.wrappedRepository = wrappedRepository;
        this.appUserRepository = appUserRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.readingProgressRepository = readingProgressRepository;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the most recent wrapped card for a user.
     * Generates the previous week's card on-demand if it doesn't exist yet.
     */
    @Transactional
    public Optional<WeeklyWrapped> getLatestWrapped(UUID userId) {
        // Try to ensure last week's card exists before returning the latest.
        ZoneId zone = resolveZone(userId);
        LocalDate lastWeekStart = LocalDate.now(zone)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1);

        ensureWrappedExists(userId, lastWeekStart, zone);

        return wrappedRepository.findFirstByUser_IdOrderByWeekStartDesc(userId);
    }

    /**
     * Returns all wrapped cards for a user, newest first.
     */
    @Transactional(readOnly = true)
    public List<WeeklyWrapped> getWrappedHistory(UUID userId) {
        return wrappedRepository.findAllByUser_IdOrderByWeekStartDesc(userId);
    }

    /**
     * Generates (or regenerates) a wrapped card for a specific ISO week.
     * The weekStart must be a Monday.
     * Idempotent: if a card already exists it is overwritten with fresh data.
     */
    @Transactional
    public WeeklyWrapped generateForUser(UUID userId, LocalDate weekStart) {
        if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException(
                    "weekStart must be a Monday. Received: " + weekStart);
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        ZoneId zone = resolveZone(user);
        LocalDate weekEnd = weekStart.plusDays(6); // Sunday

        Instant periodStart = weekStart.atStartOfDay(zone).toInstant();
        Instant periodEnd = weekEnd.plusDays(1).atStartOfDay(zone).toInstant();

        // --- sessions ---
        List<ReadingSession> sessions = readingSessionRepository
                .findSessionsOverlappingPeriod(userId, periodStart, periodEnd);

        long totalReadingSeconds = 0;
        long longestSessionSeconds = 0;
        long[] hourBuckets = new long[24];
        Set<LocalDate> activeDays = new HashSet<>();
        Map<String, Long> secondsPerBible = new HashMap<>();

        for (ReadingSession session : sessions) {
            if (session.getEndedAt() == null) continue; // skip active sessions

            Instant sStart = session.getStartedAt();
            Instant sEnd = session.getEndedAt();

            Instant effStart = sStart.isBefore(periodStart) ? periodStart : sStart;
            Instant effEnd = sEnd.isAfter(periodEnd) ? periodEnd : sEnd;

            if (!effEnd.isAfter(effStart)) continue;

            long durationSec = Duration.between(effStart, effEnd).getSeconds();
            totalReadingSeconds += durationSec;
            longestSessionSeconds = Math.max(longestSessionSeconds, durationSec);

            // Attribute reading days by local date of session start.
            activeDays.add(sStart.atZone(zone).toLocalDate());

            // Bible usage.
            if (session.getBibleId() != null) {
                secondsPerBible.merge(session.getBibleId(), durationSec, Long::sum);
            }

            // Distribute seconds across local hours.
            Instant cursor = effStart;
            while (cursor.isBefore(effEnd)) {
                Instant hourStart = cursor.atZone(zone)
                        .withMinute(0).withSecond(0).withNano(0)
                        .toInstant();
                Instant nextHour = hourStart.plus(Duration.ofHours(1));
                Instant segEnd = effEnd.isBefore(nextHour) ? effEnd : nextHour;

                int hour = cursor.atZone(zone).getHour();
                hourBuckets[hour] += Duration.between(cursor, segEnd).getSeconds();
                cursor = segEnd;
            }
        }

        int readingDays = activeDays.size();

        // Peak reading hour — the local hour with the most reading seconds.
        Integer peakReadingHour = null;
        if (totalReadingSeconds > 0) {
            long max = -1;
            for (int h = 0; h < 24; h++) {
                if (hourBuckets[h] > max) {
                    max = hourBuckets[h];
                    peakReadingHour = h;
                }
            }
        }

        // Top Bible translation.
        String topBibleId = secondsPerBible.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        // Top Bible book — derived from reading progress records in this week's period.
        String topBookId = computeTopBook(userId, periodStart, periodEnd);

        // Chapters read — distinct chapter marks updated in this week.
        int chaptersRead = countChaptersReadInPeriod(userId, periodStart, periodEnd);

        // Milestone logic.
        String milestone = computeMilestone(userId, weekStart, readingDays, totalReadingSeconds, chaptersRead);

        // Persist (upsert: delete existing then save fresh).
        wrappedRepository.findByUser_IdAndWeekStart(userId, weekStart)
                .ifPresent(wrappedRepository::delete);
        wrappedRepository.flush(); // ensure delete is visible before insert

        WeeklyWrapped wrapped = new WeeklyWrapped();
        wrapped.setUser(user);
        wrapped.setWeekStart(weekStart);
        wrapped.setWeekEnd(weekEnd);
        wrapped.setChaptersRead(chaptersRead);
        wrapped.setTotalReadingSeconds(totalReadingSeconds);
        wrapped.setReadingDays(readingDays);
        wrapped.setLongestSessionSeconds(longestSessionSeconds);
        wrapped.setPeakReadingHour(peakReadingHour);
        wrapped.setTopBibleId(topBibleId);
        wrapped.setTopBookId(topBookId);
        wrapped.setMilestone(milestone);

        return wrappedRepository.save(wrapped);
    }

    /**
     * Called by the scheduled job. Generates wrapped for every user for the
     * given week if not already generated.
     */
    @Transactional
    public void generateForAllUsers(LocalDate weekStart) {
        List<AppUser> users = appUserRepository.findAll();
        log.info("Generating weekly wrapped for {} users, week starting {}",
                users.size(), weekStart);

        for (AppUser user : users) {
            try {
                if (!wrappedRepository.existsByUser_IdAndWeekStart(user.getId(), weekStart)) {
                    generateForUser(user.getId(), weekStart);
                }
            } catch (Exception e) {
                log.error("Failed to generate wrapped for user {}: {}",
                        user.getId(), e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Generates a card for weekStart if one doesn't exist yet. */
    private void ensureWrappedExists(UUID userId, LocalDate weekStart, ZoneId zone) {
        if (!wrappedRepository.existsByUser_IdAndWeekStart(userId, weekStart)) {
            try {
                generateForUser(userId, weekStart);
            } catch (Exception e) {
                log.warn("Could not auto-generate wrapped for user {} week {}: {}",
                        userId, weekStart, e.getMessage());
            }
        }
    }

    /**
     * Milestone hierarchy (only the highest applicable milestone is awarded):
     *
     * FIRST_WEEK        – this is the user's very first wrapped card
     * SEVEN_DAY_STREAK  – read all 7 days this week
     * MARATHON_READER   – read 5+ hours (18 000 seconds) this week
     * BOOKWORM          – read 10+ chapters this week
     * CONSISTENT        – read 5+ of 7 days this week
     */
    private String computeMilestone(
            UUID userId,
            LocalDate weekStart,
            int readingDays,
            long totalReadingSeconds,
            int chaptersRead
    ) {
        // FIRST_WEEK: no prior wrapped cards exist for this user.
        long existingCards = wrappedRepository.findAllByUser_IdOrderByWeekStartDesc(userId)
                .stream()
                .filter(w -> w.getWeekStart().isBefore(weekStart))
                .count();

        if (existingCards == 0) return "FIRST_WEEK";
        if (readingDays == 7)   return "SEVEN_DAY_STREAK";
        if (totalReadingSeconds >= 18_000) return "MARATHON_READER"; // 5 h
        if (chaptersRead >= 10) return "BOOKWORM";
        if (readingDays >= 5)   return "CONSISTENT";

        return null;
    }

    /**
     * Counts distinct chapters whose readAt timestamp falls within the period.
     * We use readAt (last-marked time) as a proxy for when the chapter was completed.
     */
    private int countChaptersReadInPeriod(
            UUID userId,
            Instant periodStart,
            Instant periodEnd
    ) {
        return (int) readingProgressRepository
                .findAllByUser_IdOrderByReadAtDesc(userId)
                .stream()
                .filter(p -> !p.getReadAt().isBefore(periodStart)
                        && p.getReadAt().isBefore(periodEnd))
                .count();
    }

    /**
     * Extracts the most-read Bible book from chapter IDs that look like
     * "GEN.1", "JHN.3", "REV.22" (the API.Bible chapter ID format).
     * Returns null if nothing can be determined.
     */
    private String computeTopBook(
            UUID userId,
            Instant periodStart,
            Instant periodEnd
    ) {
        Map<String, Long> bookCounts = new HashMap<>();

        readingProgressRepository
                .findAllByUser_IdOrderByReadAtDesc(userId)
                .stream()
                .filter(p -> !p.getReadAt().isBefore(periodStart)
                        && p.getReadAt().isBefore(periodEnd))
                .forEach(p -> {
                    String bookId = extractBookId(p.getChapterId());
                    if (bookId != null) {
                        bookCounts.merge(bookId, 1L, Long::sum);
                    }
                });

        return bookCounts.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Extracts the book portion from an API.Bible chapter ID.
     * E.g. "GEN.1" → "GEN", "JHN.3.16" → "JHN", "plain" → null.
     */
    private String extractBookId(String chapterId) {
        if (chapterId == null) return null;
        int dot = chapterId.indexOf('.');
        return dot > 0 ? chapterId.substring(0, dot) : null;
    }

    private ZoneId resolveZone(UUID userId) {
        return appUserRepository.findById(userId)
                .map(this::resolveZone)
                .orElse(ZoneId.of("UTC"));
    }

    private ZoneId resolveZone(AppUser user) {
        try {
            return ZoneId.of(user.getTimezone());
        } catch (Exception e) {
            return ZoneId.of("UTC");
        }
    }
}
