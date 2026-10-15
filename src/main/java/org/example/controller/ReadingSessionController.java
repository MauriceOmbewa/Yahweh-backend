
package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.EndReadingSessionRequest;
import org.example.dto.ReadingSessionResponse;
import org.example.dto.StartReadingSessionRequest;
import org.example.model.ReadingSession;
import org.example.service.ReadingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reading-sessions")
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;

    public ReadingSessionController(
            ReadingSessionService readingSessionService
    ) {
        this.readingSessionService = readingSessionService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingSessionResponse startSession(
            @Valid @RequestBody StartReadingSessionRequest request,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ReadingSession session = readingSessionService.startSession(
                userId,
                request.bibleId()
        );

        return ReadingSessionResponse.from(session);
    }

    @PostMapping("/end")
    public ReadingSessionResponse endSession(
            @Valid @RequestBody EndReadingSessionRequest request,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        ReadingSession session = readingSessionService.endSession(
                userId,
                request.sessionId()
        );

        return ReadingSessionResponse.from(session);
    }

    @GetMapping
    public List<ReadingSessionResponse> getSessionHistory(
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingSessionService.getSessionHistory(userId)
                .stream()
                .map(ReadingSessionResponse::from)
                .toList();
    }

    @GetMapping("/{sessionId}")
    public ReadingSessionResponse getSession(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {
        UUID userId = getAuthenticatedUserId(authentication);

        return ReadingSessionResponse.from(
                readingSessionService.getSession(userId, sessionId)
        );
    }

    @GetMapping("/count")
    public long getTotalSessionCount(Authentication authentication) {
        UUID userId = getAuthenticatedUserId(authentication);

        return readingSessionService.getTotalSessionCount(userId);
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }

        return UUID.fromString(authentication.getName());
    }
}
