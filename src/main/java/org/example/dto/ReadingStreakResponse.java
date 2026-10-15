
package org.example.dto;

import java.time.LocalDate;

public record ReadingStreakResponse(
        int currentStreak,
        int longestStreak,
        long totalReadingDays,
        LocalDate lastReadingDate
) {
}
