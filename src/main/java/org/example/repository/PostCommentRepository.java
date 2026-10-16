package org.example.repository;

import org.example.model.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    Page<PostComment> findAllByPost_IdOrderByCreatedAtAsc(UUID postId, Pageable pageable);

    Optional<PostComment> findByIdAndUser_Id(UUID commentId, UUID userId);

    long countByPost_Id(UUID postId);
}
