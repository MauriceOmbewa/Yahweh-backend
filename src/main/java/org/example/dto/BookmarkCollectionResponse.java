
package org.example.dto;

import org.example.model.BookmarkCollection;

import java.time.Instant;
import java.util.UUID;

public record BookmarkCollectionResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {

    public static BookmarkCollectionResponse from(
            BookmarkCollection collection
    ) {
        return new BookmarkCollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}
