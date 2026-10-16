package org.example.repository;

import org.example.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    /** All posts by a specific user, newest first. */
    Page<Post> findAllByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Friend feed: posts from a list of friend IDs (FRIENDS_ONLY or PUBLIC)
     * plus the current user's own posts, newest first.
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.user.id IN :friendIds
           OR p.user.id = :userId
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findFeedForUser(
            @Param("userId") UUID userId,
            @Param("friendIds") List<UUID> friendIds,
            Pageable pageable
    );
}
