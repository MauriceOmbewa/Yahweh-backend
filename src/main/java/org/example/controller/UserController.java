package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.example.dto.UpdateProfileRequest;
import org.example.dto.UpdateStatusVerseRequest;
import org.example.dto.UpdateTimezoneRequest;
import org.example.dto.UserResponse;
import org.example.model.AppUser;
import org.example.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final AppUserService appUserService;

    public UserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @Operation(
            summary = "Update display name and avatar URL",
            description = "Updates the authenticated user's display name and/or avatar URL. "
                    + "Omit a field to leave it unchanged. "
                    + "Send an empty string for avatarUrl to remove it."
    )
    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AppUser user = appUserService.updateProfile(
                userId,
                request.displayName(),
                request.avatarUrl()
        );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @Operation(
            summary = "Update timezone preference",
            description = "Sets the user's local timezone used for streak and daily goal calculations. "
                    + "Accepts any IANA timezone ID, e.g. 'Africa/Nairobi' or 'America/New_York'."
    )
    @PutMapping("/timezone")
    public ResponseEntity<UserResponse> updateTimezone(
            @Valid @RequestBody UpdateTimezoneRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AppUser user = appUserService.updateTimezone(
                userId,
                request.timezone()
        );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @Operation(
            summary = "Set status verse",
            description = "Sets the verse shown on the user's public profile as their current status, "
                    + "along with an optional caption (max 280 characters)."
    )
    @PutMapping("/status")
    public ResponseEntity<UserResponse> updateStatusVerse(
            @Valid @RequestBody UpdateStatusVerseRequest request,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AppUser user = appUserService.updateStatusVerse(
                userId,
                request.bibleId(),
                request.verseId(),
                request.verseText(),
                request.caption()
        );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @Operation(
            summary = "Clear status verse",
            description = "Removes the user's current status verse and caption."
    )
    @DeleteMapping("/status")
    public ResponseEntity<UserResponse> clearStatusVerse(
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);

        AppUser user = appUserService.clearStatusVerse(userId);

        return ResponseEntity.ok(UserResponse.from(user));
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
