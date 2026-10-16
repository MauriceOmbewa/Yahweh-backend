package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.example.dto.WeeklyWrappedResponse;
import org.example.model.WeeklyWrapped;
import org.example.service.WeeklyWrappedService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Weekly Wrapped", description = "Spotify-style weekly reading summaries with milestones")
@RestController
@RequestMapping("/api/wrapped")
public class WeeklyWrappedController {

    private final WeeklyWrappedService weeklyWrappedService;

    public WeeklyWrappedController(WeeklyWrappedService weeklyWrappedService) {
        this.weeklyWrappedService = weeklyWrappedService;
    }

    @Operation(
            summary = "Get your latest Weekly Wrapped",
            description = "Returns the most recent Weekly Wrapped card. "
                    + "If last week's card hasn't been generated yet it is computed on-demand "
                    + "and returned immediately. Returns 204 if the user has no reading "
                    + "history at all."
    )
    @GetMapping("/latest")
    public ResponseEntity<WeeklyWrappedResponse> getLatest(Authentication authentication) {
        UUID userId = getUserId(authentication);

        Optional<WeeklyWrapped> wrapped = weeklyWrappedService.getLatestWrapped(userId);

        return wrapped
                .map(w -> ResponseEntity.ok(WeeklyWrappedResponse.from(w)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Get your Weekly Wrapped history",
            description = "Returns all Weekly Wrapped cards for the authenticated user, newest first."
    )
    @GetMapping("/history")
    public List<WeeklyWrappedResponse> getHistory(Authentication authentication) {
        UUID userId = getUserId(authentication);

        return weeklyWrappedService.getWrappedHistory(userId)
                .stream()
                .map(WeeklyWrappedResponse::from)
                .toList();
    }

    @Operation(
            summary = "Generate (or regenerate) a Weekly Wrapped card",
            description = "Manually triggers generation of a Wrapped card for a specific ISO week. "
                    + "The date must be a Monday (the start of the week). "
                    + "Defaults to the most recently completed week if no date is supplied. "
                    + "Existing cards are overwritten with fresh data. "
                    + "Useful for testing and backfilling."
    )
    @PostMapping("/generate")
    public ResponseEntity<WeeklyWrappedResponse> generate(
            @Parameter(description = "Start of the ISO week to generate (must be a Monday). "
                    + "Defaults to the Monday of the most recently completed week.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        if (weekStart == null) {
            // Default: the Monday of last week.
            weekStart = LocalDate.now(ZoneOffset.UTC)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .minusWeeks(1);
        }

        WeeklyWrapped wrapped = weeklyWrappedService.generateForUser(userId, weekStart);
        return ResponseEntity.ok(WeeklyWrappedResponse.from(wrapped));
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
