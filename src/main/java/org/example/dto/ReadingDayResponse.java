
package org.example.dto;

import java.time.LocalDate;

public record ReadingDayResponse(
        LocalDate date,
        boolean read,
        long completedSessions
) {
}
