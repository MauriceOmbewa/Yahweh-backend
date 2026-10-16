package org.example.scheduler;

import org.example.service.WeeklyWrappedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;

/**
 * Runs every Monday at 00:05 UTC and generates Weekly Wrapped cards for
 * all users for the ISO week that just ended (the previous Mon–Sun).
 *
 * The five-minute offset after midnight ensures the DB has settled before
 * the job runs, and avoids a race with any last-second session ends.
 *
 * Cron expression: second minute hour day month weekday
 *   0 5 0 * * MON  →  every Monday at 00:05:00 UTC
 */
@Component
public class WeeklyWrappedScheduler {

    private static final Logger log = LoggerFactory.getLogger(WeeklyWrappedScheduler.class);

    private final WeeklyWrappedService weeklyWrappedService;

    public WeeklyWrappedScheduler(WeeklyWrappedService weeklyWrappedService) {
        this.weeklyWrappedService = weeklyWrappedService;
    }

    @Scheduled(cron = "0 5 0 * * MON", zone = "UTC")
    public void generateWeeklyWrapped() {
        // The week that just ended is the one starting last Monday (7 days ago).
        LocalDate lastWeekStart = LocalDate.now(ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1);

        log.info("WeeklyWrappedScheduler: generating cards for week starting {}", lastWeekStart);

        weeklyWrappedService.generateForAllUsers(lastWeekStart);

        log.info("WeeklyWrappedScheduler: done for week starting {}", lastWeekStart);
    }
}
