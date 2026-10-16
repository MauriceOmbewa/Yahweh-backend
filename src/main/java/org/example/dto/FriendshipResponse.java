package org.example.dto;

import org.example.model.Friendship;

import java.time.Instant;
import java.util.UUID;

public record FriendshipResponse(
        UUID id,
        UserSummary requester,
        UserSummary addressee,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public record UserSummary(UUID id, String displayName, String avatarUrl) {}

    public static FriendshipResponse from(Friendship f) {
        return new FriendshipResponse(
                f.getId(),
                new UserSummary(
                        f.getRequester().getId(),
                        f.getRequester().getDisplayName(),
                        f.getRequester().getAvatarUrl()),
                new UserSummary(
                        f.getAddressee().getId(),
                        f.getAddressee().getDisplayName(),
                        f.getAddressee().getAvatarUrl()),
                f.getStatus().name(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
