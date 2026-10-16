package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.dto.PagedResponse;
import org.example.dto.UserProfileResponse;
import org.example.model.AppUser;
import org.example.service.UserProfileService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Operation(
            summary = "Get a user's public profile",
            description = "Returns display name, avatar, status verse, and (for friends/self) the current reading streak."
    )
    @GetMapping("/{userId}/profile")
    public UserProfileResponse getProfile(
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID viewerId = getUserId(authentication);
        AppUser user = userProfileService.getProfile(userId, viewerId);
        Integer streak = userProfileService.getStreakForViewer(userId, viewerId);
        return UserProfileResponse.from(user, streak);
    }

    @Operation(
            summary = "Search users by display name",
            description = "Case-insensitive partial match. Returns paginated results. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping("/search")
    public PagedResponse<UserProfileResponse> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID viewerId = getUserId(authentication);
        Page<AppUser> results = userProfileService.searchUsers(q, viewerId, page, size);

        return PagedResponse.from(results, user -> {
            Integer streak = userProfileService.getStreakForViewer(user.getId(), viewerId);
            return UserProfileResponse.from(user, streak);
        });
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
