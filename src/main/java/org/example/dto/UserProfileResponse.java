package org.example.dto;

import org.example.model.AppUser;

import java.time.Instant;
import java.util.UUID;

/**
 * Public-facing profile shown to other users.
 * Streak is only populated when the viewer is a friend (or viewing own profile).
 */
public record UserProfileResponse(
        UUID id,
        String displayName,
        String avatarUrl,
        UserResponse.StatusVerseResponse statusVerse,
        Integer currentStreak,          // null when not a friend
        Instant joinedAt
) {
    public static UserProfileResponse from(AppUser user, Integer currentStreak) {
        UserResponse.StatusVerseResponse statusVerse = null;
        if (user.getStatusVerseId() != null) {
            statusVerse = new UserResponse.StatusVerseResponse(
                    user.getStatusBibleId(),
                    user.getStatusVerseId(),
                    user.getStatusVerseText(),
                    user.getStatusCaption(),
                    user.getStatusUpdatedAt()
            );
        }
        return new UserProfileResponse(
                user.getId(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                statusVerse,
                currentStreak,
                user.getCreatedAt()
        );
    }
}
