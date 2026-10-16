package org.example.service;

import org.example.model.AppUser;
import org.example.model.UserBlock;
import org.example.repository.AppUserRepository;
import org.example.repository.UserBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final AppUserRepository appUserRepository;

    public UserBlockService(
            UserBlockRepository userBlockRepository,
            AppUserRepository appUserRepository
    ) {
        this.userBlockRepository = userBlockRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public void block(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("You cannot block yourself.");
        }

        if (userBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId)) {
            throw new IllegalArgumentException("You have already blocked this user.");
        }

        AppUser blocker = findUser(blockerId);
        AppUser blocked = findUser(blockedId);

        userBlockRepository.save(new UserBlock(blocker, blocked));
    }

    @Transactional
    public void unblock(UUID blockerId, UUID blockedId) {
        if (!userBlockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId)) {
            throw new IllegalArgumentException("You have not blocked this user.");
        }
        userBlockRepository.deleteByBlocker_IdAndBlocked_Id(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public List<AppUser> getBlockedUsers(UUID blockerId) {
        return userBlockRepository.findAllByBlocker_Id(blockerId)
                .stream()
                .map(UserBlock::getBlocked)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(UUID userA, UUID userB) {
        return userBlockRepository.existsBlockInEitherDirection(userA, userB);
    }

    /** Returns the full set of user IDs that are in a block relationship with userId. */
    @Transactional(readOnly = true)
    public Set<UUID> getBlockedUserIds(UUID userId) {
        return userBlockRepository.findAllBlockedUserIds(userId)
                .stream()
                .collect(Collectors.toSet());
    }

    private AppUser findUser(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}
