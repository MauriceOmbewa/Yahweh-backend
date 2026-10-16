package org.example.dto;

import org.example.model.ContentReport;

import java.time.Instant;
import java.util.UUID;

public record ContentReportResponse(
        UUID id,
        UUID reporterId,
        UUID reportedPostId,    // null when reporting a user
        UUID reportedUserId,    // null when reporting a post
        String reason,
        String description,
        String status,
        UUID reviewedBy,
        Instant reviewedAt,
        Instant createdAt
) {
    public static ContentReportResponse from(ContentReport r) {
        return new ContentReportResponse(
                r.getId(),
                r.getReporter().getId(),
                r.getReportedPost() != null ? r.getReportedPost().getId() : null,
                r.getReportedUser() != null ? r.getReportedUser().getId() : null,
                r.getReason().name(),
                r.getDescription(),
                r.getStatus().name(),
                r.getReviewedBy() != null ? r.getReviewedBy().getId() : null,
                r.getReviewedAt(),
                r.getCreatedAt()
        );
    }
}
