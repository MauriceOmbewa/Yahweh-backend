
package org.example.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddVerseToCollectionRequest(

        @NotNull(message = "Verse bookmark ID is required.")
        UUID verseBookmarkId

) {
}
