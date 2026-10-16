package org.example.service;

import org.example.model.AppUser;
import org.example.model.Notification;
import org.example.model.NotificationType;
import org.example.repository.AppUserRepository;
import org.example.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final AppUserRepository appUserRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            AppUserRepository appUserRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.appUserRepository = appUserRepository;
    }

    // -------------------------------------------------------------------------
    // Internal — called by other services to dispatch notifications
    // -------------------------------------------------------------------------

    /**
     * Creates a notification. Silently swallows errors so that a notification
     * failure never rolls back the primary action that triggered it.
     */
    @Transactional
    public void dispatch(
            UUID recipientId,
            NotificationType type,
            UUID actorId,
            UUID referenceId
    ) {
        // Never notify yourself.
        if (recipientId.equals(actorId)) return;

        try {
            AppUser recipient = appUserRepository.findById(recipientId).orElse(null);
            if (recipient == null) return;

            AppUser actor = (actorId != null)
                    ? appUserRepository.findById(actorId).orElse(null)
                    : null;

            Notification notification = new Notification();
            notification.setUser(recipient);
            notification.setType(type);
            notification.setActor(actor);
            notification.setReferenceId(referenceId);

            notificationRepository.save(notification);

        } catch (Exception e) {
            log.warn("Failed to dispatch notification type={} to userId={}: {}",
                    type, recipientId, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // User-facing queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(UUID userId, int page, int size) {
        return notificationRepository.findAllByUser_IdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found."));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
