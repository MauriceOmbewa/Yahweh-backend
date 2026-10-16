package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.dto.AddVerseToCollectionRequest;
import org.example.dto.BookmarkCollectionRequest;
import org.example.dto.BookmarkCollectionResponse;
import org.example.dto.PagedResponse;
import org.example.dto.VerseBookmarkResponse;
import org.example.model.BookmarkCollection;
import org.example.service.BookmarkCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Bookmark Collections", description = "Organise saved verses into named collections")
@RestController
@RequestMapping("/api/bookmark-collections")
public class BookmarkCollectionController {

    private final BookmarkCollectionService collectionService;

    public BookmarkCollectionController(BookmarkCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Operation(summary = "Create a new bookmark collection")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookmarkCollectionResponse createCollection(
            @Valid @RequestBody BookmarkCollectionRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        BookmarkCollection collection = collectionService.createCollection(
                userId, request.name(), request.description());
        return BookmarkCollectionResponse.from(collection);
    }

    @Operation(summary = "Get all bookmark collections")
    @GetMapping
    public List<BookmarkCollectionResponse> getUserCollections(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return collectionService.getUserCollections(userId)
                .stream()
                .map(BookmarkCollectionResponse::from)
                .toList();
    }

    @Operation(summary = "Get a single bookmark collection")
    @GetMapping("/{collectionId}")
    public BookmarkCollectionResponse getCollection(
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return BookmarkCollectionResponse.from(collectionService.getCollection(userId, collectionId));
    }

    @Operation(summary = "Update a bookmark collection's name or description")
    @PutMapping("/{collectionId}")
    public BookmarkCollectionResponse updateCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody BookmarkCollectionRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        BookmarkCollection collection = collectionService.updateCollection(
                userId, collectionId, request.name(), request.description());
        return BookmarkCollectionResponse.from(collection);
    }

    @Operation(summary = "Delete a bookmark collection")
    @DeleteMapping("/{collectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(
            @PathVariable UUID collectionId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        collectionService.deleteCollection(userId, collectionId);
    }

    @Operation(summary = "Add a saved verse to a collection")
    @PostMapping("/{collectionId}/verses")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addVerseToCollection(
            @PathVariable UUID collectionId,
            @Valid @RequestBody AddVerseToCollectionRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        collectionService.addVerseToCollection(userId, collectionId, request.verseBookmarkId());
    }

    @Operation(
            summary = "Get verses in a collection",
            description = "Returns a paginated list of verses in this collection, newest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping("/{collectionId}/verses")
    public PagedResponse<VerseBookmarkResponse> getCollectionVerses(
            @PathVariable UUID collectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return PagedResponse.from(
                collectionService.getCollectionVerses(userId, collectionId, page, size),
                VerseBookmarkResponse::from
        );
    }

    @Operation(summary = "Remove a verse from a collection")
    @DeleteMapping("/{collectionId}/verses/{verseBookmarkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeVerseFromCollection(
            @PathVariable UUID collectionId,
            @PathVariable UUID verseBookmarkId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        collectionService.removeVerseFromCollection(userId, collectionId, verseBookmarkId);
    }
}
