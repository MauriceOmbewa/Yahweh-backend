
package org.example.repository;

import org.example.model.ReadingProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadingProgressRepository
        extends JpaRepository<ReadingProgress, UUID> {

    Optional<ReadingProgress> findByUser_IdAndBibleIdAndChapterId(
            UUID userId,
            String bibleId,
            String chapterId
    );

    List<ReadingProgress> findAllByUser_IdOrderByReadAtDesc(UUID userId);

    Page<ReadingProgress> findAllByUser_IdOrderByReadAtDesc(UUID userId, Pageable pageable);

    boolean existsByUser_IdAndBibleIdAndChapterId(
            UUID userId,
            String bibleId,
            String chapterId
    );

    long countByUser_Id(UUID userId);
}
