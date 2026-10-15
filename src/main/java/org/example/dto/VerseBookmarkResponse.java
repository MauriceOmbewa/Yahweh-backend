
package org.example.dto;

import org.example.model.VerseBookmark;

import java.time.Instant;
import java.util.UUID;

public record VerseBookmarkResponse(
        UUID id,
        String bibleId,
        String verseId,
        String highlightColor,
        String note,
        Instant createdAt,
        Instant updatedAt
) {

    public static VerseBookmarkResponse from(VerseBookmark bookmark) {
        return new VerseBookmarkResponse(
                bookmark.getId(),
                bookmark.getBibleId(),
                bookmark.getVerseId(),
                bookmark.getHighlightColor(),
                bookmark.getNote(),
                bookmark.getCreatedAt(),
                bookmark.getUpdatedAt()
        );
    }
}
