package org.example.repository;

import org.example.model.Friendship;
import org.example.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    /** Find the friendship row regardless of which side initiated it. */
    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.requester.id = :userA AND f.addressee.id = :userB)
           OR (f.requester.id = :userB AND f.addressee.id = :userA)
    """)
    Optional<Friendship> findByUserPair(
            @Param("userA") UUID userA,
            @Param("userB") UUID userB
    );

    /** All accepted friends for a user (either side of the relationship). */
    @Query("""
        SELECT f FROM Friendship f
        WHERE f.status = 'ACCEPTED'
          AND (f.requester.id = :userId OR f.addressee.id = :userId)
        ORDER BY f.updatedAt DESC
    """)
    List<Friendship> findAcceptedFriendships(@Param("userId") UUID userId);

    /** Incoming PENDING requests where this user is the addressee. */
    List<Friendship> findAllByAddressee_IdAndStatus(UUID addresseeId, FriendshipStatus status);

    /** Outgoing PENDING requests sent by this user. */
    List<Friendship> findAllByRequester_IdAndStatus(UUID requesterId, FriendshipStatus status);

    /** Check existence by exact requester→addressee direction. */
    boolean existsByRequester_IdAndAddressee_Id(UUID requesterId, UUID addresseeId);

    /** IDs of users who are accepted friends of the given user. */
    @Query("""
        SELECT CASE WHEN f.requester.id = :userId THEN f.addressee.id
                    ELSE f.requester.id END
        FROM Friendship f
        WHERE f.status = 'ACCEPTED'
          AND (f.requester.id = :userId OR f.addressee.id = :userId)
    """)
    List<UUID> findFriendIds(@Param("userId") UUID userId);
}
