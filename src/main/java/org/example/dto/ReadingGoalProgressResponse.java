
package org.example.dto;

import java.time.LocalDate;

public record ReadingGoalProgressResponse(
        LocalDate date,
        int dailyGoalMinutes,
        long totalReadingSeconds,
        long remainingSeconds,
        double progressPercentage,
        boolean goalReached
) {
}
