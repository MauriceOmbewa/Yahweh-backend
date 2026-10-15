
package org.example.dto;

public record ReadingActivityByHourResponse(
        int hour,
        long sessionCount,
        long totalReadingSeconds
) {
}
