package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.dto.PagedResponse;
import org.example.dto.SaveVerseBookmarkRequest;
import org.example.dto.VerseBookmarkResponse;
import org.example.model.VerseBookmark;
import org.example.service.VerseBookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Bookmarks", description = "Save, highlight, and annotate Bible verses")
@RestController
@RequestMapping("/api/bookmarks")
public class VerseBookmarkController {

    private final VerseBookmarkService verseBookmarkService;

    public VerseBookmarkController(VerseBookmarkService verseBookmarkService) {
        this.verseBookmarkService = verseBookmarkService;
    }

    @Operation(summary = "Save or update a verse bookmark")
    @PostMapping("/{bibleId}/{verseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public VerseBookmarkResponse saveVerse(
            @PathVariable String bibleId,
            @PathVariable String verseId,
            @Valid @RequestBody(required = false) SaveVerseBookmarkRequest request,
            Authentication authentication
    ) {
        SaveVerseBookmarkRequest safeRequest =
                request == null ? new SaveVerseBookmarkRequest(null, null) : request;

        UUID userId = UUID.fromString(authentication.getName());

        VerseBookmark bookmark = verseBookmarkService.saveVerse(
                userId, bibleId, verseId,
                safeRequest.highlightColor(),
                safeRequest.note()
        );

        return VerseBookmarkResponse.from(bookmark);
    }

    @Operation(
            summary = "Get all saved verses",
            description = "Returns a paginated list of saved verse bookmarks, newest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping
    public PagedResponse<VerseBookmarkResponse> getUserVerses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return PagedResponse.from(
                verseBookmarkService.getUserVerses(userId, page, size),
                VerseBookmarkResponse::from
        );
    }

    @Operation(summary = "Get a single saved verse")
    @GetMapping("/{bibleId}/{verseId}")
    public VerseBookmarkResponse getVerse(
            @PathVariable String bibleId,
            @PathVariable String verseId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return VerseBookmarkResponse.from(verseBookmarkService.getVerse(userId, bibleId, verseId));
    }

    @Operation(summary = "Delete a saved verse bookmark")
    @DeleteMapping("/{bibleId}/{verseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVerse(
            @PathVariable String bibleId,
            @PathVariable String verseId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        verseBookmarkService.deleteVerse(userId, bibleId, verseId);
    }
}
