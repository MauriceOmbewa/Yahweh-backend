package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.dto.EndReadingSessionRequest;
import org.example.dto.PagedResponse;
import org.example.dto.ReadingSessionResponse;
import org.example.dto.StartReadingSessionRequest;
import org.example.model.ReadingSession;
import org.example.service.ReadingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reading-sessions")
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;

    public ReadingSessionController(ReadingSessionService readingSessionService) {
        this.readingSessionService = readingSessionService;
    }

    @Operation(summary = "Start a new reading session")
    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingSessionResponse startSession(
            @Valid @RequestBody StartReadingSessionRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        ReadingSession session = readingSessionService.startSession(userId, request.bibleId());
        return ReadingSessionResponse.from(session);
    }

    @Operation(summary = "End an active reading session")
    @PostMapping("/end")
    public ReadingSessionResponse endSession(
            @Valid @RequestBody EndReadingSessionRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        ReadingSession session = readingSessionService.endSession(userId, request.sessionId());
        return ReadingSessionResponse.from(session);
    }

    @Operation(
            summary = "Get reading session history",
            description = "Returns a paginated list of reading sessions, newest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping
    public PagedResponse<ReadingSessionResponse> getSessionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        return PagedResponse.from(
                readingSessionService.getSessionHistory(userId, page, size),
                ReadingSessionResponse::from
        );
    }

    @Operation(summary = "Get a single reading session by ID")
    @GetMapping("/{sessionId}")
    public ReadingSessionResponse getSession(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        return ReadingSessionResponse.from(readingSessionService.getSession(userId, sessionId));
    }

    @Operation(summary = "Get total reading session count")
    @GetMapping("/count")
    public long getTotalSessionCount(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return readingSessionService.getTotalSessionCount(userId);
    }

    private UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        return UUID.fromString(authentication.getName());
    }
}
