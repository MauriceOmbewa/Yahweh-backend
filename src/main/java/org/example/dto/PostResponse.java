package org.example.dto;

import org.example.model.Post;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostResponse(
        UUID id,
        AuthorSummary author,
        String bibleId,
        String verseId,
        String verseText,
        String caption,
        String visibility,
        long commentCount,
        long totalReactions,
        Map<String, Long> reactionCounts,   // e.g. {"AMEN": 3, "HEART": 1}
        String viewerReaction,              // reaction type the requesting user gave, or null
        Instant createdAt,
        Instant updatedAt
) {
    public record AuthorSummary(UUID id, String displayName, String avatarUrl) {}

    /** Minimal constructor used when reaction/comment data is not yet loaded. */
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                new AuthorSummary(
                        post.getUser().getId(),
                        post.getUser().getDisplayName(),
                        post.getUser().getAvatarUrl()),
                post.getBibleId(),
                post.getVerseId(),
                post.getVerseText(),
                post.getCaption(),
                post.getVisibility().name(),
                0L,
                0L,
                Map.of(),
                null,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    /** Full constructor including social data. */
    public static PostResponse from(
            Post post,
            long commentCount,
            long totalReactions,
            Map<String, Long> reactionCounts,
            String viewerReaction
    ) {
        return new PostResponse(
                post.getId(),
                new AuthorSummary(
                        post.getUser().getId(),
                        post.getUser().getDisplayName(),
                        post.getUser().getAvatarUrl()),
                post.getBibleId(),
                post.getVerseId(),
                post.getVerseText(),
                post.getCaption(),
                post.getVisibility().name(),
                commentCount,
                totalReactions,
                reactionCounts,
                viewerReaction,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
