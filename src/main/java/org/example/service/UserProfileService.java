package org.example.service;

import org.example.dto.ReadingStreakResponse;
import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserProfileService {

    private final AppUserRepository appUserRepository;
    private final FriendshipService friendshipService;
    private final ReadingStreakService readingStreakService;
    private final UserBlockService userBlockService;

    public UserProfileService(
            AppUserRepository appUserRepository,
            FriendshipService friendshipService,
            ReadingStreakService readingStreakService,
            UserBlockService userBlockService
    ) {
        this.appUserRepository = appUserRepository;
        this.friendshipService = friendshipService;
        this.readingStreakService = readingStreakService;
        this.userBlockService = userBlockService;
    }

    /**
     * Returns the public profile of a user.
     * Throws if the viewer has been blocked by the target or vice versa.
     */
    @Transactional(readOnly = true)
    public AppUser getProfile(UUID targetUserId, UUID viewerId) {
        AppUser target = appUserRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!targetUserId.equals(viewerId)
                && userBlockService.isBlocked(viewerId, targetUserId)) {
            throw new IllegalArgumentException("User not found.");
        }

        return target;
    }

    /**
     * Returns the streak for a user if the viewer is allowed to see it.
     * Returns null otherwise.
     */
    @Transactional(readOnly = true)
    public Integer getStreakForViewer(UUID targetUserId, UUID viewerId) {
        boolean canSeeStreak = targetUserId.equals(viewerId)
                || friendshipService.areFriends(viewerId, targetUserId);

        if (!canSeeStreak) return null;

        ReadingStreakResponse streak = readingStreakService.getStreak(targetUserId);
        return streak.currentStreak();
    }

    /**
     * Searches users by display name (case-insensitive, partial match).
     * Excludes the searching user and any blocked users from results.
     */
    @Transactional(readOnly = true)
    public Page<AppUser> searchUsers(String query, UUID excludeUserId, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank.");
        }
        if (query.length() > 100) {
            throw new IllegalArgumentException("Search query must not exceed 100 characters.");
        }

        String normalized = query.trim().toLowerCase();
        Set<UUID> blockedIds = userBlockService.getBlockedUserIds(excludeUserId);

        List<AppUser> matches = appUserRepository.findAll().stream()
                .filter(u -> !u.getId().equals(excludeUserId))
                .filter(u -> !blockedIds.contains(u.getId()))
                .filter(u -> u.getDisplayName().toLowerCase().contains(normalized))
                .toList();

        int start = page * size;
        int end = Math.min(start + size, matches.size());

        List<AppUser> pageContent = (start >= matches.size())
                ? List.of()
                : matches.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(page, size), matches.size());
    }
}
