
package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BookmarkCollectionVerseId implements Serializable {

    @Column(name = "collection_id")
    private UUID collectionId;

    @Column(name = "verse_bookmark_id")
    private UUID verseBookmarkId;

    public BookmarkCollectionVerseId() {
    }

    public BookmarkCollectionVerseId(
            UUID collectionId,
            UUID verseBookmarkId
    ) {
        this.collectionId = collectionId;
        this.verseBookmarkId = verseBookmarkId;
    }

    public UUID getCollectionId() {
        return collectionId;
    }

    public UUID getVerseBookmarkId() {
        return verseBookmarkId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof BookmarkCollectionVerseId that)) {
            return false;
        }

        return Objects.equals(collectionId, that.collectionId)
                && Objects.equals(verseBookmarkId, that.verseBookmarkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, verseBookmarkId);
    }
}
