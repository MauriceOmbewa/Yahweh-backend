package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.example.dto.NotificationResponse;
import org.example.dto.PagedResponse;
import org.example.model.Notification;
import org.example.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "Get notifications",
            description = "Returns paginated notifications for the authenticated user, newest first. "
                    + "Defaults to page 0, 20 items per page."
    )
    @GetMapping
    public PagedResponse<NotificationResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        Page<Notification> notifications = notificationService.getNotifications(userId, page, size);
        return PagedResponse.from(notifications, NotificationResponse::from);
    }

    @Operation(summary = "Get unread notification count (badge count)")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        UUID userId = getUserId(authentication);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @Operation(summary = "Mark a single notification as read")
    @PutMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication
    ) {
        UUID userId = getUserId(authentication);
        return NotificationResponse.from(
                notificationService.markAsRead(notificationId, userId));
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UUID userId = getUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
