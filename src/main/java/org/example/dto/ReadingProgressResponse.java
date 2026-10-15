
package org.example.dto;

import org.example.model.ReadingProgress;

import java.time.Instant;
import java.util.UUID;

public record ReadingProgressResponse(
        UUID id,
        String bibleId,
        String chapterId,
        Instant readAt,
        Instant createdAt
) {

    public static ReadingProgressResponse from(ReadingProgress progress) {
        return new ReadingProgressResponse(
                progress.getId(),
                progress.getBibleId(),
                progress.getChapterId(),
                progress.getReadAt(),
                progress.getCreatedAt()
        );
    }
}
