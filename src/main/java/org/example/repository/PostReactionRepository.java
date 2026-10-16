package org.example.repository;

import org.example.model.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface PostReactionRepository extends JpaRepository<PostReaction, UUID> {

    Optional<PostReaction> findByPost_IdAndUser_Id(UUID postId, UUID userId);

    boolean existsByPost_IdAndUser_Id(UUID postId, UUID userId);

    void deleteByPost_IdAndUser_Id(UUID postId, UUID userId);

    List<PostReaction> findAllByPost_Id(UUID postId);

    long countByPost_Id(UUID postId);

    /** Count per reaction_type for a post: returns [{reactionType, count}]. */
    @Query("""
        SELECT r.reactionType AS reactionType, COUNT(r) AS count
        FROM PostReaction r
        WHERE r.post.id = :postId
        GROUP BY r.reactionType
    """)
    List<ReactionCount> countByTypeForPost(@Param("postId") UUID postId);

    interface ReactionCount {
        String getReactionType();
        long getCount();
    }
}
