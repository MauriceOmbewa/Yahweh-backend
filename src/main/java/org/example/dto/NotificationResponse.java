package org.example.dto;

import org.example.model.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        ActorSummary actor,     // null for system events
        UUID referenceId,
        boolean read,
        Instant createdAt
) {
    public record ActorSummary(UUID id, String displayName, String avatarUrl) {}

    public static NotificationResponse from(Notification n) {
        ActorSummary actor = null;
        if (n.getActor() != null) {
            actor = new ActorSummary(
                    n.getActor().getId(),
                    n.getActor().getDisplayName(),
                    n.getActor().getAvatarUrl()
            );
        }
        return new NotificationResponse(
                n.getId(),
                n.getType().name(),
                actor,
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
