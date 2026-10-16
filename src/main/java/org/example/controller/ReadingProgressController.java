package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.dto.PagedResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reading-progress")
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    public ReadingProgressController(ReadingProgressService readingProgressService) {
        this.readingProgressService = readingProgressService;
    }

    @Operation(summary = "Mark a chapter as read")
    @PostMapping("/{bibleId}/chapters/{chapterId}")
    public ResponseEntity<ReadingProgressResponse> markChapterAsRead(
            @PathVariable String bibleId,
            @PathVariable String chapterId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        ReadingProgress progress = readingProgressService.markChapterAsRead(userId, bibleId, chapterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReadingProgressResponse.from(progress));
    }

    @Operation(
            summary = "Get reading history",
            description = "Returns a paginated list of chapters marked as read, newest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping
    public ResponseEntity<PagedResponse<ReadingProgressResponse>> getReadingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        UUID userId = getUserId(authentication);

        PagedResponse<ReadingProgressResponse> response = PagedResponse.from(
                readingProgressService.getReadingHistory(userId, page, size),
                ReadingProgressResponse::from
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get total chapters read count")
    @GetMapping("/count")
    public ResponseEntity<Long> getReadingCount(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return ResponseEntity.ok(readingProgressService.countReadChapters(userId));
    }

    @Operation(summary = "Check whether a specific chapter has been read")
    @GetMapping("/{bibleId}/chapters/{chapterId}")
    public ResponseEntity<ReadingProgressStatusResponse> getChapterStatus(
            @PathVariable String bibleId,
            @PathVariable String chapterId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        boolean read = readingProgressService.hasReadChapter(userId, bibleId, chapterId);
        return ResponseEntity.ok(new ReadingProgressStatusResponse(bibleId, chapterId, read));
    }

    @Operation(summary = "Mark a chapter as unread")
    @DeleteMapping("/{bibleId}/chapters/{chapterId}")
    public ResponseEntity<Void> markChapterAsUnread(
            @PathVariable String bibleId,
            @PathVariable String chapterId,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        readingProgressService.markChapterAsUnread(userId, bibleId, chapterId);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
