
package org.example.controller;

import org.example.dto.ReadingStatisticsResponse;
import org.example.service.ReadingStatisticsService;
import org.example.dto.ReadingActivityByHourResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reading-statistics")
public class ReadingStatisticsController {

    private final ReadingStatisticsService readingStatisticsService;

    public ReadingStatisticsController(
            ReadingStatisticsService readingStatisticsService
    ) {
        this.readingStatisticsService = readingStatisticsService;
    }

    @GetMapping
    public ReadingStatisticsResponse getStatistics(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant end,

            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingStatisticsService.getStatistics(
                userId,
                start,
                end
        );
    }

    @GetMapping("/weekly")
    public ReadingStatisticsResponse getWeeklyStatistics(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        Instant start = now
                .toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant end = start.plusSeconds(7 * 24 * 60 * 60);

        return readingStatisticsService.getStatistics(
                userId,
                start,
                end
        );
    }

    @GetMapping("/daily")
    public ReadingStatisticsResponse getDailyStatistics(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        Instant start = now
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant end = start.plusSeconds(24 * 60 * 60);

        return readingStatisticsService.getStatistics(
                userId,
                start,
                end
        );
    }

    @GetMapping("/monthly")
    public ReadingStatisticsResponse getMonthlyStatistics(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        Instant start = now
                .toLocalDate()
                .withDayOfMonth(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant end = now
                .toLocalDate()
                .withDayOfMonth(1)
                .plusMonths(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        return readingStatisticsService.getStatistics(
                userId,
                start,
                end
        );
    }

    @GetMapping("/activity-by-hour")
    public List<ReadingActivityByHourResponse> getActivityByHour(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant end,

            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingStatisticsService.getActivityByHour(
                userId,
                start,
                end
        );
    }

    @GetMapping("/weekly/activity-by-hour")
    public List<ReadingActivityByHourResponse> getWeeklyActivityByHour(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        Instant start = now
                .toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant end = start.plusSeconds(7 * 24 * 60 * 60);

        return readingStatisticsService.getActivityByHour(
                userId,
                start,
                end
        );
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }

        return UUID.fromString(authentication.getName());
    }
}
