
package org.example.controller;

import org.example.dto.ReadingStreakResponse;
import org.example.dto.ReadingDayResponse;
import org.example.service.ReadingStreakService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;

@Tag(name = "Reading Streak", description = "Current streak, longest streak, and reading calendar")
@RestController
@RequestMapping("/api/reading-streak")
public class ReadingStreakController {

    private final ReadingStreakService readingStreakService;

    public ReadingStreakController(
            ReadingStreakService readingStreakService
    ) {
        this.readingStreakService = readingStreakService;
    }

    @GetMapping
    public ReadingStreakResponse getStreak(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingStreakService.getStreak(userId);
    }

    @GetMapping("/calendar")
    public List<ReadingDayResponse> getReadingCalendar(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,

            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingStreakService.getReadingCalendar(
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
