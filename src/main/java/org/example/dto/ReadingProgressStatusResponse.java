
package org.example.dto;

public record ReadingProgressStatusResponse(
        String bibleId,
        String chapterId,
        boolean read
) {
}
