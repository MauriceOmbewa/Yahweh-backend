package org.example.service;

import org.example.model.AppUser;
import org.example.model.Friendship;
import org.example.model.FriendshipStatus;
import org.example.model.NotificationType;
import org.example.repository.AppUserRepository;
import org.example.repository.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;

    public FriendshipService(
            FriendshipRepository friendshipRepository,
            AppUserRepository appUserRepository,
            NotificationService notificationService
    ) {
        this.friendshipRepository = friendshipRepository;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------------
    // Send / manage requests
    // -------------------------------------------------------------------------

    @Transactional
    public Friendship sendRequest(UUID requesterId, UUID addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself.");
        }

        AppUser requester = findUser(requesterId);
        AppUser addressee = findUser(addresseeId);

        // Check for any existing relationship between this pair.
        Optional<Friendship> existing = friendshipRepository.findByUserPair(requesterId, addresseeId);

        if (existing.isPresent()) {
            Friendship f = existing.get();
            switch (f.getStatus()) {
                case ACCEPTED  -> throw new IllegalArgumentException("You are already friends.");
                case PENDING   -> throw new IllegalArgumentException("A friend request is already pending.");
                case BLOCKED   -> throw new IllegalArgumentException("This action is not allowed.");
                case DECLINED  -> {
                    // Allow re-sending after a decline: reset the row.
                    f.setRequester(requester);
                    f.setAddressee(addressee);
                    f.setStatus(FriendshipStatus.PENDING);
                    return friendshipRepository.save(f);
                }
            }
        }

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        Friendship saved = friendshipRepository.save(friendship);

        // Notify the addressee that they have a new friend request.
        notificationService.dispatch(
                addresseeId, NotificationType.FRIEND_REQUEST,
                requesterId, saved.getId());

        return saved;
    }

    @Transactional
    public Friendship acceptRequest(UUID friendshipId, UUID currentUserId) {
        Friendship friendship = getFriendshipById(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only accept requests sent to you.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("This request is no longer pending.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendshipRepository.save(friendship);

        // Notify the original requester that their request was accepted.
        notificationService.dispatch(
                friendship.getRequester().getId(),
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                currentUserId, saved.getId());

        return saved;
    }

    @Transactional
    public Friendship declineRequest(UUID friendshipId, UUID currentUserId) {
        Friendship friendship = getFriendshipById(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only decline requests sent to you.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("This request is no longer pending.");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public void unfriend(UUID currentUserId, UUID otherUserId) {
        Friendship friendship = friendshipRepository
                .findByUserPair(currentUserId, otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("No friendship found."));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalArgumentException("You are not friends with this user.");
        }

        friendshipRepository.delete(friendship);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Friendship> getFriends(UUID userId) {
        return friendshipRepository.findAcceptedFriendships(userId);
    }

    @Transactional(readOnly = true)
    public List<Friendship> getIncomingRequests(UUID userId) {
        return friendshipRepository.findAllByAddressee_IdAndStatus(
                userId, FriendshipStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Friendship> getOutgoingRequests(UUID userId) {
        return friendshipRepository.findAllByRequester_IdAndStatus(
                userId, FriendshipStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Optional<FriendshipStatus> getRelationshipStatus(UUID userA, UUID userB) {
        return friendshipRepository.findByUserPair(userA, userB)
                .map(Friendship::getStatus);
    }

    @Transactional(readOnly = true)
    public boolean areFriends(UUID userA, UUID userB) {
        return friendshipRepository.findByUserPair(userA, userB)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<UUID> getFriendIds(UUID userId) {
        return friendshipRepository.findFriendIds(userId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AppUser findUser(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private Friendship getFriendshipById(UUID friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found."));
    }
}
