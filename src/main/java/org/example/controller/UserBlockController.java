package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.dto.UserProfileResponse;
import org.example.model.AppUser;
import org.example.service.UserBlockService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Block", description = "Block and unblock users; view your blocked list")
@RestController
@RequestMapping("/api/users")
public class UserBlockController {

    private final UserBlockService userBlockService;

    public UserBlockController(UserBlockService userBlockService) {
        this.userBlockService = userBlockService;
    }

    @Operation(
            summary = "Block a user",
            description = "Blocked users are hidden from your feed, search results, and cannot see your profile."
    )
    @PostMapping("/{userId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockUser(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID currentUserId = getUserId(authentication);
        userBlockService.block(currentUserId, userId);
    }

    @Operation(summary = "Unblock a previously blocked user")
    @DeleteMapping("/{userId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblockUser(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID currentUserId = getUserId(authentication);
        userBlockService.unblock(currentUserId, userId);
    }

    @Operation(summary = "List users you have blocked")
    @GetMapping("/blocked")
    public List<UserProfileResponse> getBlockedUsers(Authentication authentication) {
        UUID currentUserId = getUserId(authentication);
        List<AppUser> blocked = userBlockService.getBlockedUsers(currentUserId);
        // Streak is not relevant in the blocked list — always null.
        return blocked.stream()
                .map(u -> UserProfileResponse.from(u, null))
                .toList();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
