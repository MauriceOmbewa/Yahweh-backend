package org.example.dto;

import org.example.model.PostComment;

import java.time.Instant;
import java.util.UUID;

public record PostCommentResponse(
        UUID id,
        UUID postId,
        AuthorSummary author,
        String body,
        Instant createdAt,
        Instant updatedAt
) {
    public record AuthorSummary(UUID id, String displayName, String avatarUrl) {}

    public static PostCommentResponse from(PostComment comment) {
        return new PostCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                new AuthorSummary(
                        comment.getUser().getId(),
                        comment.getUser().getDisplayName(),
                        comment.getUser().getAvatarUrl()),
                comment.getBody(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
