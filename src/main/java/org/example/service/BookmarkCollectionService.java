
package org.example.service;

import org.example.model.AppUser;
import org.example.model.BookmarkCollection;
import org.example.model.BookmarkCollectionVerse;
import org.example.model.VerseBookmark;
import org.example.repository.BookmarkCollectionVerseRepository;
import org.example.repository.VerseBookmarkRepository;
import org.example.repository.AppUserRepository;
import org.example.repository.BookmarkCollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BookmarkCollectionService {

    private final BookmarkCollectionRepository collectionRepository;
    private final AppUserRepository appUserRepository;

    private final BookmarkCollectionVerseRepository collectionVerseRepository;
    private final VerseBookmarkRepository verseBookmarkRepository;

    public BookmarkCollectionService(
            BookmarkCollectionRepository collectionRepository,
            AppUserRepository appUserRepository,
            BookmarkCollectionVerseRepository collectionVerseRepository,
            VerseBookmarkRepository verseBookmarkRepository
    ) {
        this.collectionRepository = collectionRepository;
        this.appUserRepository = appUserRepository;
        this.collectionVerseRepository = collectionVerseRepository;
        this.verseBookmarkRepository = verseBookmarkRepository;
    }

    @Transactional
    public BookmarkCollection createCollection(
            UUID userId,
            String name,
            String description
    ) {
        String normalizedName = normalizeName(name);
        String normalizedDescription = normalizeDescription(description);

        if (collectionRepository.existsByUser_IdAndNameIgnoreCase(
                userId, normalizedName
        )) {
            throw new IllegalArgumentException(
                    "You already have a collection with this name."
            );
        }

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found."
                ));

        BookmarkCollection collection = new BookmarkCollection();
        collection.setUser(user);
        collection.setName(normalizedName);
        collection.setDescription(normalizedDescription);

        return collectionRepository.save(collection);
    }

    @Transactional(readOnly = true)
    public List<BookmarkCollection> getUserCollections(UUID userId) {
        return collectionRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public BookmarkCollection getCollection(
            UUID userId,
            UUID collectionId
    ) {
        return collectionRepository.findByIdAndUser_Id(
                collectionId, userId
        ).orElseThrow(() -> new IllegalArgumentException(
                "Collection not found."
        ));
    }

    @Transactional
    public BookmarkCollection updateCollection(
            UUID userId,
            UUID collectionId,
            String name,
            String description
    ) {
        BookmarkCollection collection = getCollection(userId, collectionId);

        String normalizedName = normalizeName(name);
        String normalizedDescription = normalizeDescription(description);

        boolean nameChanged = !collection.getName()
                .equalsIgnoreCase(normalizedName);

        if (nameChanged
                && collectionRepository.existsByUser_IdAndNameIgnoreCase(
                userId, normalizedName
        )) {
            throw new IllegalArgumentException(
                    "You already have a collection with this name."
            );
        }

        collection.setName(normalizedName);
        collection.setDescription(normalizedDescription);

        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(UUID userId, UUID collectionId) {
        BookmarkCollection collection = getCollection(userId, collectionId);
        collectionRepository.delete(collection);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Collection name is required."
            );
        }

        String normalized = name.trim();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Collection name must not exceed 100 characters."
            );
        }

        return normalized;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String normalized = description.trim();

        if (normalized.length() > 500) {
            throw new IllegalArgumentException(
                    "Description must not exceed 500 characters."
            );
        }

        return normalized;
    }

    @Transactional
    public void addVerseToCollection(
            UUID userId,
            UUID collectionId,
            UUID verseBookmarkId
    ) {
        BookmarkCollection collection = getCollection(userId, collectionId);

        VerseBookmark bookmark = verseBookmarkRepository
                .findById(verseBookmarkId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Saved verse not found."
                ));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Saved verse not found."
            );
        }

        boolean alreadyAdded =
                collectionVerseRepository
                        .existsByCollection_IdAndVerseBookmark_Id(
                                collectionId,
                                verseBookmarkId
                        );

        if (alreadyAdded) {
            return;
        }

        BookmarkCollectionVerse relationship =
                new BookmarkCollectionVerse(collection, bookmark);

        collectionVerseRepository.save(relationship);
    }

    @Transactional(readOnly = true)
    public List<VerseBookmark> getCollectionVerses(
            UUID userId,
            UUID collectionId
    ) {
        getCollection(userId, collectionId);

        return collectionVerseRepository
                .findAllByCollection_IdOrderByAddedAtDesc(collectionId)
                .stream()
                .map(BookmarkCollectionVerse::getVerseBookmark)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<VerseBookmark> getCollectionVerses(
            UUID userId,
            UUID collectionId,
            int page,
            int size
    ) {
        getCollection(userId, collectionId);

        Pageable pageable = PageRequest.of(page, size);
        return collectionVerseRepository
                .findAllByCollection_IdOrderByAddedAtDesc(collectionId, pageable)
                .map(BookmarkCollectionVerse::getVerseBookmark);
    }

    @Transactional
    public void removeVerseFromCollection(
            UUID userId,
            UUID collectionId,
            UUID verseBookmarkId
    ) {
        getCollection(userId, collectionId);

        BookmarkCollectionVerse relationship =
                collectionVerseRepository
                        .findByCollection_IdAndVerseBookmark_Id(
                                collectionId,
                                verseBookmarkId
                        )
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Verse was not found in this collection."
                        ));

        if (!relationship.getVerseBookmark().getUser().getId()
                .equals(userId)) {
            throw new IllegalArgumentException(
                    "Verse was not found in this collection."
            );
        }

        collectionVerseRepository.delete(relationship);
    }
}
