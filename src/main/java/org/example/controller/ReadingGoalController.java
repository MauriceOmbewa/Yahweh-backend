
package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.ReadingGoalRequest;
import org.example.dto.ReadingGoalResponse;
import org.example.dto.ReadingGoalProgressResponse;
import org.example.model.ReadingGoal;
import org.example.service.ReadingGoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reading-goals")
public class ReadingGoalController {

    private final ReadingGoalService readingGoalService;

    public ReadingGoalController(ReadingGoalService readingGoalService) {
        this.readingGoalService = readingGoalService;
    }

    @GetMapping
    public ReadingGoalResponse getGoal(Authentication authentication) {
        UUID userId = getAuthenticatedUserId(authentication);

        ReadingGoal goal = readingGoalService.getOrCreateGoal(userId);

        return ReadingGoalResponse.from(goal);
    }

    @PutMapping
    public ReadingGoalResponse updateGoal(
            @Valid @RequestBody ReadingGoalRequest request,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ReadingGoal goal = readingGoalService.updateGoal(
                userId,
                request.dailyGoalMinutes()
        );

        return ReadingGoalResponse.from(goal);
    }

    @GetMapping("/progress/today")
    public ReadingGoalProgressResponse getTodayProgress(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingGoalService.getTodayProgress(userId);
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
