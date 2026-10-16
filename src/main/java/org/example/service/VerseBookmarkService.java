
package org.example.service;

import org.example.model.AppUser;
import org.example.model.VerseBookmark;
import org.example.repository.AppUserRepository;
import org.example.repository.VerseBookmarkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class VerseBookmarkService {

    private final VerseBookmarkRepository verseBookmarkRepository;
    private final AppUserRepository appUserRepository;

    public VerseBookmarkService(
            VerseBookmarkRepository verseBookmarkRepository,
            AppUserRepository appUserRepository
    ) {
        this.verseBookmarkRepository = verseBookmarkRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public VerseBookmark saveVerse(
            UUID userId,
            String bibleId,
            String verseId,
            String highlightColor,
            String note
    ) {
        String normalizedBibleId = normalizeId(bibleId, "Bible ID");
        String normalizedVerseId = normalizeId(verseId, "Verse ID");

        String normalizedColor = normalizeOptionalText(highlightColor);
        String normalizedNote = normalizeOptionalText(note);

        if (normalizedColor != null && normalizedColor.length() > 20) {
            throw new IllegalArgumentException(
                    "Highlight color must not exceed 20 characters."
            );
        }

        if (normalizedNote != null && normalizedNote.length() > 10000) {
            throw new IllegalArgumentException(
                    "Note must not exceed 10000 characters."
            );
        }

        VerseBookmark bookmark = verseBookmarkRepository
                .findByUser_IdAndBibleIdAndVerseId(
                        userId,
                        normalizedBibleId,
                        normalizedVerseId
                )
                .orElseGet(() -> {
                    AppUser user = appUserRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "User not found."
                            ));

                    VerseBookmark newBookmark = new VerseBookmark();
                    newBookmark.setUser(user);
                    newBookmark.setBibleId(normalizedBibleId);
                    newBookmark.setVerseId(normalizedVerseId);

                    return newBookmark;
                });

        bookmark.setHighlightColor(normalizedColor);
        bookmark.setNote(normalizedNote);

        return verseBookmarkRepository.save(bookmark);
    }

    @Transactional(readOnly = true)
    public List<VerseBookmark> getUserVerses(UUID userId) {
        return verseBookmarkRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Page<VerseBookmark> getUserVerses(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return verseBookmarkRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public VerseBookmark getVerse(
            UUID userId,
            String bibleId,
            String verseId
    ) {
        String normalizedBibleId = normalizeId(bibleId, "Bible ID");
        String normalizedVerseId = normalizeId(verseId, "Verse ID");

        return verseBookmarkRepository
                .findByUser_IdAndBibleIdAndVerseId(
                        userId,
                        normalizedBibleId,
                        normalizedVerseId
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Saved verse not found."
                ));
    }

    @Transactional
    public void deleteVerse(
            UUID userId,
            String bibleId,
            String verseId
    ) {
        VerseBookmark bookmark = getVerse(userId, bibleId, verseId);
        verseBookmarkRepository.delete(bookmark);
    }

    private String normalizeId(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
