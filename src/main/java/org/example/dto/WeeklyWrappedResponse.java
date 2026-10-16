package org.example.dto;

import org.example.model.WeeklyWrapped;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record WeeklyWrappedResponse(
        UUID id,
        LocalDate weekStart,
        LocalDate weekEnd,

        // Reading metrics
        int chaptersRead,
        long totalReadingSeconds,
        int readingDays,
        long longestSessionSeconds,
        Integer peakReadingHour,       // null when no sessions

        // Top content
        String topBibleId,
        String topBookId,

        // Engagement badge
        String milestone,              // null when no milestone reached
        String milestoneLabel,         // human-readable, e.g. "7-Day Streak 🔥"

        // Derived convenience fields
        long totalReadingMinutes,
        double averageDailyMinutes,    // totalReadingSeconds / 60 / readingDays, 0 if no days

        Instant createdAt
) {

    public static WeeklyWrappedResponse from(WeeklyWrapped wrapped) {
        long totalMinutes = wrapped.getTotalReadingSeconds() / 60;
        double avgDailyMinutes = wrapped.getReadingDays() == 0
                ? 0.0
                : Math.round((totalMinutes * 10.0) / wrapped.getReadingDays()) / 10.0;

        return new WeeklyWrappedResponse(
                wrapped.getId(),
                wrapped.getWeekStart(),
                wrapped.getWeekEnd(),
                wrapped.getChaptersRead(),
                wrapped.getTotalReadingSeconds(),
                wrapped.getReadingDays(),
                wrapped.getLongestSessionSeconds(),
                wrapped.getPeakReadingHour(),
                wrapped.getTopBibleId(),
                wrapped.getTopBookId(),
                wrapped.getMilestone(),
                milestoneLabel(wrapped.getMilestone()),
                totalMinutes,
                avgDailyMinutes,
                wrapped.getCreatedAt()
        );
    }

    private static String milestoneLabel(String milestone) {
        if (milestone == null) return null;
        return switch (milestone) {
            case "FIRST_WEEK"      -> "First Wrapped! 🎉";
            case "SEVEN_DAY_STREAK"-> "7-Day Streak 🔥";
            case "MARATHON_READER" -> "Marathon Reader 📖";
            case "BOOKWORM"        -> "Bookworm 🐛";
            case "CONSISTENT"      -> "Consistent Reader ✨";
            default                -> milestone;
        };
    }
}
