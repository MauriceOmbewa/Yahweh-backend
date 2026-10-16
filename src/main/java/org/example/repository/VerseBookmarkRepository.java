
package org.example.repository;

import org.example.model.VerseBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VerseBookmarkRepository
        extends JpaRepository<VerseBookmark, UUID> {

    Optional<VerseBookmark> findByUser_IdAndBibleIdAndVerseId(
            UUID userId,
            String bibleId,
            String verseId
    );

    List<VerseBookmark> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    Page<VerseBookmark> findAllByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    boolean existsByUser_IdAndBibleIdAndVerseId(
            UUID userId,
            String bibleId,
            String verseId
    );
}
