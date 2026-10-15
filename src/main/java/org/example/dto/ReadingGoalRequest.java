
package org.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReadingGoalRequest(

        @NotNull(message = "Daily reading goal is required.")
        @Min(value = 1, message = "Reading goal must be at least 1 minute.")
        @Max(value = 1440, message = "Reading goal cannot exceed 1440 minutes.")
        Integer dailyGoalMinutes

) {
}
