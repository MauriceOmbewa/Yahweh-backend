package org.example.repository;

import org.example.model.UserBlock;
import org.example.model.UserBlockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {

    boolean existsByBlocker_IdAndBlocked_Id(UUID blockerId, UUID blockedId);

    void deleteByBlocker_IdAndBlocked_Id(UUID blockerId, UUID blockedId);

    /** All users blocked by the given user. */
    List<UserBlock> findAllByBlocker_Id(UUID blockerId);

    /**
     * Returns true if either direction of a block exists between the two users.
     * Used to enforce mutual exclusion in feeds and profiles.
     */
    @Query("""
        SELECT COUNT(b) > 0 FROM UserBlock b
        WHERE (b.blocker.id = :userA AND b.blocked.id = :userB)
           OR (b.blocker.id = :userB AND b.blocked.id = :userA)
    """)
    boolean existsBlockInEitherDirection(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    /** IDs of all users that :userId has blocked or that have blocked :userId. */
    @Query("""
        SELECT CASE WHEN b.blocker.id = :userId THEN b.blocked.id
                    ELSE b.blocker.id END
        FROM UserBlock b
        WHERE b.blocker.id = :userId OR b.blocked.id = :userId
    """)
    List<UUID> findAllBlockedUserIds(@Param("userId") UUID userId);
}
