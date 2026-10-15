
package org.example.repository;

import org.example.model.BookmarkCollectionVerse;
import org.example.model.BookmarkCollectionVerseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkCollectionVerseRepository
        extends JpaRepository<BookmarkCollectionVerse, BookmarkCollectionVerseId> {

    List<BookmarkCollectionVerse>
    findAllByCollection_IdOrderByAddedAtDesc(UUID collectionId);

    Optional<BookmarkCollectionVerse>
    findByCollection_IdAndVerseBookmark_Id(
            UUID collectionId,
            UUID verseBookmarkId
    );

    boolean existsByCollection_IdAndVerseBookmark_Id(
            UUID collectionId,
            UUID verseBookmarkId
    );

    void deleteByCollection_IdAndVerseBookmark_Id(
            UUID collectionId,
            UUID verseBookmarkId
    );
}
