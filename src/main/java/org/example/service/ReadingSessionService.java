
package org.example.service;

import org.example.model.AppUser;
import org.example.model.ReadingSession;
import org.example.repository.AppUserRepository;
import org.example.repository.ReadingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final AppUserRepository appUserRepository;

    public ReadingSessionService(
            ReadingSessionRepository readingSessionRepository,
            AppUserRepository appUserRepository
    ) {
        this.readingSessionRepository = readingSessionRepository;
        this.appUserRepository = appUserRepository;
    }


    @Transactional
    public ReadingSession startSession(UUID userId, String bibleId) {
        String normalizedBibleId = normalizeBibleId(bibleId);

        // Lock this user's database row to serialize concurrent start requests.
        AppUser user = appUserRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Instant now = Instant.now();

        // Close any existing active session before starting another.
        readingSessionRepository
                .findFirstByUser_IdAndEndedAtIsNullOrderByStartedAtDesc(userId)
                .ifPresent(existingSession -> {
                    existingSession.setEndedAt(now);
                    readingSessionRepository.save(existingSession);
                });

        ReadingSession session = new ReadingSession();
        session.setUser(user);
        session.setBibleId(normalizedBibleId);
        session.setStartedAt(now);

        return readingSessionRepository.save(session);
    }

    @Transactional
    public ReadingSession endSession(UUID userId, UUID sessionId) {
        ReadingSession session = readingSessionRepository
                .findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Reading session not found."));

        if (session.getEndedAt() != null) {
            throw new IllegalArgumentException(
                    "This reading session has already ended."
            );
        }

        Instant endTime = Instant.now();

        if (endTime.isBefore(session.getStartedAt())) {
            throw new IllegalArgumentException(
                    "Session end time cannot be before its start time."
            );
        }

        session.setEndedAt(endTime);
        return readingSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<ReadingSession> getSessionHistory(UUID userId) {
        return readingSessionRepository
                .findAllByUser_IdOrderByStartedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public ReadingSession getSession(UUID userId, UUID sessionId) {
        return readingSessionRepository
                .findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Reading session not found."));
    }

    @Transactional(readOnly = true)
    public long getTotalSessionCount(UUID userId) {
        return readingSessionRepository.countByUser_Id(userId);
    }

    private String normalizeBibleId(String bibleId) {
        if (bibleId == null || bibleId.isBlank()) {
            throw new IllegalArgumentException("Bible ID is required.");
        }

        String normalized = bibleId.trim();

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Bible ID cannot exceed 100 characters."
            );
        }

        return normalized;
    }
}
