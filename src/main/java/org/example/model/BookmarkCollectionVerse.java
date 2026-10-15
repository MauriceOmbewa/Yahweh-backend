
package org.example.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "bookmark_collection_verses")
public class BookmarkCollectionVerse {

    @EmbeddedId
    private BookmarkCollectionVerseId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("collectionId")
    @JoinColumn(name = "collection_id", nullable = false)
    private BookmarkCollection collection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("verseBookmarkId")
    @JoinColumn(name = "verse_bookmark_id", nullable = false)
    private VerseBookmark verseBookmark;

    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    public BookmarkCollectionVerse() {
    }

    public BookmarkCollectionVerse(
            BookmarkCollection collection,
            VerseBookmark verseBookmark
    ) {
        this.collection = collection;
        this.verseBookmark = verseBookmark;
        this.id = new BookmarkCollectionVerseId(
                collection.getId(),
                verseBookmark.getId()
        );
    }

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }

    public BookmarkCollectionVerseId getId() {
        return id;
    }

    public BookmarkCollection getCollection() {
        return collection;
    }

    public VerseBookmark getVerseBookmark() {
        return verseBookmark;
    }

    public Instant getAddedAt() {
        return addedAt;
    }
}
