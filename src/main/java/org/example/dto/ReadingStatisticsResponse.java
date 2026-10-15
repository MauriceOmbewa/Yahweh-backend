
package org.example.dto;

import java.time.Instant;

public record ReadingStatisticsResponse(
        Instant periodStart,
        Instant periodEnd,
        long totalReadingSeconds,
        long totalSessions,
        long averageSessionSeconds,
        long completedSessions,
        long activeSessions
) {
}
