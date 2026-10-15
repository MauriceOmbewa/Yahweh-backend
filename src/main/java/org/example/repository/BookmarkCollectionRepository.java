
package org.example.repository;

import org.example.model.BookmarkCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookmarkCollectionRepository
        extends JpaRepository<BookmarkCollection, UUID> {

    List<BookmarkCollection> findAllByUser_IdOrderByCreatedAtDesc(
            UUID userId
    );

    Optional<BookmarkCollection> findByIdAndUser_Id(
            UUID collectionId,
            UUID userId
    );

    boolean existsByUser_IdAndNameIgnoreCase(
            UUID userId,
            String name
    );
}
