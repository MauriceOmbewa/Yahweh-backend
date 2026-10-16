package org.example.dto;

import org.example.model.AppUser;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String displayName,
        String avatarUrl,
        String timezone,
        StatusVerseResponse statusVerse,
        Instant createdAt
) {

    public record StatusVerseResponse(
            String bibleId,
            String verseId,
            String verseText,
            String caption,
            Instant updatedAt
    ) {}

    public static UserResponse from(AppUser user) {
        StatusVerseResponse statusVerse = null;

        if (user.getStatusVerseId() != null) {
            statusVerse = new StatusVerseResponse(
                    user.getStatusBibleId(),
                    user.getStatusVerseId(),
                    user.getStatusVerseText(),
                    user.getStatusCaption(),
                    user.getStatusUpdatedAt()
            );
        }

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getTimezone(),
                statusVerse,
                user.getCreatedAt()
        );
    }
}
