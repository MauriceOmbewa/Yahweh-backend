
package org.example.controller;

import org.example.dto.ReadingProgressResponse;
import org.example.dto.ReadingProgressStatusResponse;
import org.example.model.ReadingProgress;
import org.example.service.ReadingProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reading-progress")
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    public ReadingProgressController(
            ReadingProgressService readingProgressService) {
        this.readingProgressService = readingProgressService;
    }

    @PostMapping("/{bibleId}/chapters/{chapterId}")
    public ResponseEntity<ReadingProgressResponse> markChapterAsRead(
            @PathVariable String bibleId,
            @PathVariable String chapterId,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        ReadingProgress progress =
                readingProgressService.markChapterAsRead(
                        userId, bibleId, chapterId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ReadingProgressResponse.from(progress));
    }

    @GetMapping
    public ResponseEntity<List<ReadingProgressResponse>> getReadingHistory(
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        List<ReadingProgressResponse> history =
                readingProgressService.getReadingHistory(userId)
                        .stream()
                        .map(ReadingProgressResponse::from)
                        .toList();

        return ResponseEntity.ok(history);
    }

    @GetMapping("/{bibleId}/chapters/{chapterId}")
    public ResponseEntity<ReadingProgressStatusResponse> getChapterStatus(
            @PathVariable String bibleId,
            @PathVariable String chapterId,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        boolean read = readingProgressService.hasReadChapter(
                userId, bibleId, chapterId);

        return ResponseEntity.ok(
                new ReadingProgressStatusResponse(
                        bibleId, chapterId, read));
    }

    @DeleteMapping("/{bibleId}/chapters/{chapterId}")
    public ResponseEntity<Void> markChapterAsUnread(
            @PathVariable String bibleId,
            @PathVariable String chapterId,
            Authentication authentication) {

        UUID userId = getAuthenticatedUserId(authentication);

        readingProgressService.markChapterAsUnread(
                userId, bibleId, chapterId);

        return ResponseEntity.noContent().build();
    }

    private UUID getAuthenticatedUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
