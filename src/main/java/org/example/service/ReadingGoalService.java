
package org.example.service;

import org.example.dto.ReadingGoalProgressResponse;
import org.example.model.AppUser;
import org.example.model.ReadingGoal;
import org.example.model.ReadingSession;
import org.example.repository.AppUserRepository;
import org.example.repository.ReadingGoalRepository;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReadingGoalService {

    private static final int DEFAULT_DAILY_GOAL_MINUTES = 15;

    private final ReadingGoalRepository readingGoalRepository;
    private final AppUserRepository appUserRepository;
    private final ReadingSessionRepository readingSessionRepository;

    public ReadingGoalService(
            ReadingGoalRepository readingGoalRepository,
            AppUserRepository appUserRepository,
            ReadingSessionRepository readingSessionRepository
    ) {
        this.readingGoalRepository = readingGoalRepository;
        this.appUserRepository = appUserRepository;
        this.readingSessionRepository = readingSessionRepository;
    }

    @Transactional(readOnly = true)
    public ReadingGoal getGoal(UUID userId) {
        return readingGoalRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Reading goal not found. Create a goal first."
                        ));
    }

    @Transactional
    public ReadingGoal getOrCreateGoal(UUID userId) {
        return readingGoalRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    AppUser user = appUserRepository.findById(userId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User not found."
                                    ));

                    ReadingGoal goal = new ReadingGoal();
                    goal.setUser(user);
                    goal.setDailyGoalMinutes(
                            DEFAULT_DAILY_GOAL_MINUTES
                    );

                    return readingGoalRepository.save(goal);
                });
    }

    @Transactional
    public ReadingGoal updateGoal(
            UUID userId,
            int dailyGoalMinutes
    ) {
        if (dailyGoalMinutes < 1 || dailyGoalMinutes > 1440) {
            throw new IllegalArgumentException(
                    "Daily reading goal must be between 1 and 1440 minutes."
            );
        }

        ReadingGoal goal = getOrCreateGoal(userId);
        goal.setDailyGoalMinutes(dailyGoalMinutes);

        return readingGoalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public ReadingGoalProgressResponse getTodayProgress(UUID userId) {
        // Read the saved goal without creating a database record.
        Optional<ReadingGoal> savedGoal =
                readingGoalRepository.findByUser_Id(userId);

        int dailyGoalMinutes = savedGoal
                .map(ReadingGoal::getDailyGoalMinutes)
                .orElse(DEFAULT_DAILY_GOAL_MINUTES);

        // Use the user's stored timezone so "today" matches their local day.
        ZoneId zone = appUserRepository.findById(userId)
                .map(AppUser::getTimezone)
                .map(tz -> {
                    try {
                        return ZoneId.of(tz);
                    } catch (Exception e) {
                        return ZoneId.of("UTC");
                    }
                })
                .orElse(ZoneId.of("UTC"));

        LocalDate today = LocalDate.now(zone);

        Instant startOfDay = today.atStartOfDay(zone).toInstant();
        Instant startOfNextDay = today.plusDays(1).atStartOfDay(zone).toInstant();

        Instant now = Instant.now();

        List<ReadingSession> sessions =
                readingSessionRepository.findSessionsOverlappingPeriod(
                        userId,
                        startOfDay,
                        startOfNextDay
                );

        long totalReadingSeconds = 0;

        for (ReadingSession session : sessions) {
            Instant sessionStart = session.getStartedAt();
            Instant sessionEnd = session.getEndedAt();

            Instant effectiveStart = sessionStart.isAfter(startOfDay)
                    ? sessionStart
                    : startOfDay;

            Instant effectiveEnd;

            if (sessionEnd == null) {
                effectiveEnd = now.isBefore(startOfNextDay) ? now : startOfNextDay;
            } else {
                effectiveEnd = sessionEnd.isBefore(startOfNextDay)
                        ? sessionEnd
                        : startOfNextDay;
            }

            if (effectiveEnd.isAfter(effectiveStart)) {
                totalReadingSeconds += Duration.between(effectiveStart, effectiveEnd)
                        .getSeconds();
            }
        }

        long goalSeconds = dailyGoalMinutes * 60L;
        long remainingSeconds = Math.max(0L, goalSeconds - totalReadingSeconds);
        double progressPercentage = Math.min(
                100.0,
                totalReadingSeconds * 100.0 / goalSeconds
        );
        boolean goalReached = totalReadingSeconds >= goalSeconds;

        return new ReadingGoalProgressResponse(
                today,
                dailyGoalMinutes,
                totalReadingSeconds,
                remainingSeconds,
                Math.round(progressPercentage * 100.0) / 100.0,
                goalReached
        );
    }
}
