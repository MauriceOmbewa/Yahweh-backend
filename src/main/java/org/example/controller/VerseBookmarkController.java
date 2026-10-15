
package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.SaveVerseBookmarkRequest;
import org.example.dto.VerseBookmarkResponse;
import org.example.model.VerseBookmark;
import org.example.service.VerseBookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookmarks")
public class VerseBookmarkController {

    private final VerseBookmarkService verseBookmarkService;

    public VerseBookmarkController(
            VerseBookmarkService verseBookmarkService
    ) {
        this.verseBookmarkService = verseBookmarkService;
    }

    @PostMapping("/{bibleId}/{verseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public VerseBookmarkResponse saveVerse(
            @PathVariable String bibleId,
            @PathVariable String verseId,
            @Valid @RequestBody(required = false)
            SaveVerseBookmarkRequest request,
            Authentication authentication
    ) {
        SaveVerseBookmarkRequest safeRequest =
                request == null
                        ? new SaveVerseBookmarkRequest(null, null)
                        : request;

        UUID userId = UUID.fromString(authentication.getName());

        VerseBookmark bookmark = verseBookmarkService.saveVerse(
                userId,
                bibleId,
                verseId,
                safeRequest.highlightColor(),
                safeRequest.note()
        );

        return VerseBookmarkResponse.from(bookmark);
    }

    @GetMapping
    public List<VerseBookmarkResponse> getUserVerses(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return verseBookmarkService.getUserVerses(userId)
                .stream()
                .map(VerseBookmarkResponse::from)
                .toList();
    }

    @GetMapping("/{bibleId}/{verseId}")
    public VerseBookmarkResponse getVerse(
            @PathVariable String bibleId,
            @PathVariable String verseId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return VerseBookmarkResponse.from(
                verseBookmarkService.getVerse(
                        userId,
                        bibleId,
                        verseId
                )
        );
    }

    @DeleteMapping("/{bibleId}/{verseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVerse(
            @PathVariable String bibleId,
            @PathVariable String verseId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        verseBookmarkService.deleteVerse(
                userId,
                bibleId,
                verseId
        );
    }
}
