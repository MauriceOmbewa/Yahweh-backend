
package org.example.dto;

import org.example.model.ReadingGoal;

import java.time.Instant;
import java.util.UUID;

public record ReadingGoalResponse(
        UUID id,
        int dailyGoalMinutes,
        Instant createdAt,
        Instant updatedAt
) {

    public static ReadingGoalResponse from(ReadingGoal goal) {
        return new ReadingGoalResponse(
                goal.getId(),
                goal.getDailyGoalMinutes(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}
