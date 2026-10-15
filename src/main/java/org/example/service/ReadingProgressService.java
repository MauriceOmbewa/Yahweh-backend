
package org.example.service;

import org.example.model.AppUser;
import org.example.model.ReadingProgress;
import org.example.repository.AppUserRepository;
import org.example.repository.ReadingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;
    private final AppUserRepository appUserRepository;

    public ReadingProgressService(
            ReadingProgressRepository readingProgressRepository,
            AppUserRepository appUserRepository) {
        this.readingProgressRepository = readingProgressRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public ReadingProgress markChapterAsRead(
            UUID userId,
            String bibleId,
            String chapterId) {

        String normalizedBibleId = normalizeIdentifier(bibleId, "Bible ID");
        String normalizedChapterId = normalizeIdentifier(chapterId, "Chapter ID");

        return readingProgressRepository
                .findByUser_IdAndBibleIdAndChapterId(
                        userId,
                        normalizedBibleId,
                        normalizedChapterId)
                .map(existing -> {
                    existing.setReadAt(java.time.Instant.now());
                    return readingProgressRepository.save(existing);
                })
                .orElseGet(() -> {
                    AppUser user = appUserRepository.findById(userId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException("User not found."));

                    ReadingProgress progress = new ReadingProgress();
                    progress.setUser(user);
                    progress.setBibleId(normalizedBibleId);
                    progress.setChapterId(normalizedChapterId);

                    return readingProgressRepository.save(progress);
                });
    }

    public List<ReadingProgress> getReadingHistory(UUID userId) {
        return readingProgressRepository
                .findAllByUser_IdOrderByReadAtDesc(userId);
    }

    public boolean hasReadChapter(
            UUID userId,
            String bibleId,
            String chapterId) {

        return readingProgressRepository
                .existsByUser_IdAndBibleIdAndChapterId(
                        userId,
                        normalizeIdentifier(bibleId, "Bible ID"),
                        normalizeIdentifier(chapterId, "Chapter ID"));
    }

    public long countReadChapters(UUID userId) {
        return readingProgressRepository.countByUser_Id(userId);
    }

    @Transactional
    public void markChapterAsUnread(
            UUID userId,
            String bibleId,
            String chapterId) {

        ReadingProgress progress = readingProgressRepository
                .findByUser_IdAndBibleIdAndChapterId(
                        userId,
                        normalizeIdentifier(bibleId, "Bible ID"),
                        normalizeIdentifier(chapterId, "Chapter ID"))
                .orElseThrow(() ->
                        new IllegalArgumentException("Reading progress not found."));

        readingProgressRepository.delete(progress);
    }

    private String normalizeIdentifier(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        String normalized = value.trim();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed 100 characters.");
        }

        return normalized;
    }
}
