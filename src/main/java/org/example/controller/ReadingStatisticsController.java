package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.dto.ReadingActivityByHourResponse;
import org.example.dto.ReadingStatisticsResponse;
import org.example.service.ReadingStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reading Statistics", description = "Daily, weekly, monthly stats and activity-by-hour breakdown")
@RestController
@RequestMapping("/api/reading-statistics")
public class ReadingStatisticsController {

    private final ReadingStatisticsService readingStatisticsService;

    public ReadingStatisticsController(
            ReadingStatisticsService readingStatisticsService
    ) {
        this.readingStatisticsService = readingStatisticsService;
    }

    @Operation(
            summary = "Get statistics for a custom period",
            description = "Returns reading statistics for an arbitrary period defined by explicit "
                    + "start and end timestamps (ISO-8601)."
    )
    @GetMapping
    public ReadingStatisticsResponse getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        return readingStatisticsService.getStatistics(userId, start, end);
    }

    @Operation(
            summary = "Get today's statistics",
            description = "Returns reading statistics for the current calendar day in the user's timezone."
    )
    @GetMapping("/daily")
    public ReadingStatisticsResponse getDailyStatistics(Authentication authentication) {
        return readingStatisticsService.getDailyStatistics(getUserId(authentication));
    }

    @Operation(
            summary = "Get this week's statistics",
            description = "Returns reading statistics for the current ISO week (Monday–Sunday) "
                    + "in the user's timezone."
    )
    @GetMapping("/weekly")
    public ReadingStatisticsResponse getWeeklyStatistics(Authentication authentication) {
        return readingStatisticsService.getWeeklyStatistics(getUserId(authentication));
    }

    @Operation(
            summary = "Get this month's statistics",
            description = "Returns reading statistics for the current calendar month "
                    + "in the user's timezone."
    )
    @GetMapping("/monthly")
    public ReadingStatisticsResponse getMonthlyStatistics(Authentication authentication) {
        return readingStatisticsService.getMonthlyStatistics(getUserId(authentication));
    }

    @Operation(
            summary = "Get activity by hour for a custom period",
            description = "Returns a 24-bucket breakdown of reading activity for an arbitrary period. "
                    + "Hours are expressed in the user's local timezone."
    )
    @GetMapping("/activity-by-hour")
    public List<ReadingActivityByHourResponse> getActivityByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        return readingStatisticsService.getActivityByHour(userId, start, end);
    }

    @Operation(
            summary = "Get this week's activity by hour",
            description = "Returns a 24-bucket breakdown of reading activity for the current ISO week. "
                    + "Hours are expressed in the user's local timezone."
    )
    @GetMapping("/weekly/activity-by-hour")
    public List<ReadingActivityByHourResponse> getWeeklyActivityByHour(
            Authentication authentication
    ) {
        return readingStatisticsService.getWeeklyActivityByHour(getUserId(authentication));
    }

    private UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        return UUID.fromString(authentication.getName());
    }
}
