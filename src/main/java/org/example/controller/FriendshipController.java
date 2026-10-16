package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.dto.FriendshipResponse;
import org.example.model.Friendship;
import org.example.service.FriendshipService;
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

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @Operation(summary = "Send a friend request to another user")
    @PostMapping("/request/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendshipResponse sendRequest(
            @PathVariable UUID targetUserId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Friendship f = friendshipService.sendRequest(userId, targetUserId);
        return FriendshipResponse.from(f);
    }

    @Operation(summary = "Accept an incoming friend request")
    @PostMapping("/request/{friendshipId}/accept")
    public FriendshipResponse acceptRequest(
            @PathVariable UUID friendshipId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Friendship f = friendshipService.acceptRequest(friendshipId, userId);
        return FriendshipResponse.from(f);
    }

    @Operation(summary = "Decline an incoming friend request")
    @PostMapping("/request/{friendshipId}/decline")
    public FriendshipResponse declineRequest(
            @PathVariable UUID friendshipId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Friendship f = friendshipService.declineRequest(friendshipId, userId);
        return FriendshipResponse.from(f);
    }

    @Operation(summary = "List accepted friends")
    @GetMapping
    public List<FriendshipResponse> getFriends(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return friendshipService.getFriends(userId)
                .stream().map(FriendshipResponse::from).toList();
    }

    @Operation(summary = "List incoming pending friend requests")
    @GetMapping("/requests/incoming")
    public List<FriendshipResponse> getIncomingRequests(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return friendshipService.getIncomingRequests(userId)
                .stream().map(FriendshipResponse::from).toList();
    }

    @Operation(summary = "List outgoing pending friend requests")
    @GetMapping("/requests/outgoing")
    public List<FriendshipResponse> getOutgoingRequests(Authentication authentication) {
        UUID userId = getUserId(authentication);
        return friendshipService.getOutgoingRequests(userId)
                .stream().map(FriendshipResponse::from).toList();
    }

    @Operation(summary = "Unfriend a user")
    @DeleteMapping("/{otherUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfriend(
            @PathVariable UUID otherUserId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        friendshipService.unfriend(userId, otherUserId);
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
