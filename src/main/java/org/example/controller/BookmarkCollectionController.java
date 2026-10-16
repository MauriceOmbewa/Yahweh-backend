
package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.BookmarkCollectionRequest;
import org.example.dto.BookmarkCollectionResponse;
import org.example.model.BookmarkCollection;
import org.example.service.BookmarkCollectionService;
import org.example.dto.AddVerseToCollectionRequest;
import org.example.dto.VerseBookmarkResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookmark-collections")
public class BookmarkCollectionController {

    private final BookmarkCollectionService collectionService;

    public BookmarkCollectionController(
            BookmarkCollectionService collectionService
    ) {
        this.collectionService = collectionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkCollectionResponse createCollection(
            @Valid @RequestBody BookmarkCollectionRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        BookmarkCollection collection = collectionService.createCollection(
                userId,
                request.name(),
                request.description()
        );

        return BookmarkCollectionResponse.from(collection);
    }

    @GetMapping
    public List<BookmarkCollectionResponse> getUserCollections(
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return collectionService.getUserCollections(userId)
                .stream()
                .map(BookmarkCollectionResponse::from)
                .toList();
    }

    @GetMapping("/{collectionId}")
    public BookmarkCollectionResponse getCollection(
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return BookmarkCollectionResponse.from(
                collectionService.getCollection(userId, collectionId)
        );
    }

    @PutMapping("/{collectionId}")
    public BookmarkCollectionResponse updateCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody BookmarkCollectionRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        BookmarkCollection collection = collectionService.updateCollection(
                userId,
                collectionId,
                request.name(),
                request.description()
        );

        return BookmarkCollectionResponse.from(collection);
    }

    @DeleteMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        collectionService.deleteCollection(userId, collectionId);
    }

    @PostMapping("/{collectionId}/verses")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addVerseToCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AddVerseToCollectionRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        collectionService.addVerseToCollection(
                userId,
                collectionId,
                request.verseBookmarkId()
        );
    }

    @GetMapping("/{collectionId}/verses")
    public List<VerseBookmarkResponse> getCollectionVerses(
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return collectionService.getCollectionVerses(userId, collectionId)
                .stream()
                .map(VerseBookmarkResponse::from)
                .toList();
    }

    @DeleteMapping("/{collectionId}/verses/{verseBookmarkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeVerseFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID verseBookmarkId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        collectionService.removeVerseFromCollection(
                userId,
                collectionId,
                verseBookmarkId
        );
    }

}
