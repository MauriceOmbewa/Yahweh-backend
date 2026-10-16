
package org.example.service;

import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.example.exception.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AppUser> findById(UUID id) {
        return appUserRepository.findById(id);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(normalizeEmail(email));
    }

    public boolean emailExists(String email) {
        return appUserRepository.existsByEmail(normalizeEmail(email));
    }

    @Transactional
    public AppUser createUser(
            String email,
            String displayName,
            String rawPassword
    ) {
        String normalizedEmail = normalizeEmail(email);

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists."
            );
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(displayName.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return appUserRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public AppUser authenticate(String email, String rawPassword) {
        AppUser user = findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
                        "Invalid email or password."));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid email or password.");
        }

        return user;
    }

    @Transactional
    public AppUser updateProfile(UUID userId, String displayName, String avatarUrl) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (displayName != null && !displayName.isBlank()) {
            user.setDisplayName(displayName.trim());
        }

        // avatarUrl may be explicitly set to empty string to remove it.
        if (avatarUrl != null) {
            String trimmed = avatarUrl.trim();
            user.setAvatarUrl(trimmed.isEmpty() ? null : trimmed);
        }

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateTimezone(UUID userId, String timezone) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Validate that this is a recognised IANA timezone identifier.
        try {
            ZoneId.of(timezone);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "'" + timezone + "' is not a valid timezone. "
                    + "Use an IANA timezone ID such as 'Africa/Nairobi' or 'America/New_York'.");
        }

        user.setTimezone(timezone);
        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser updateStatusVerse(
            UUID userId,
            String bibleId,
            String verseId,
            String verseText,
            String caption) {

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setStatusBibleId(bibleId.trim());
        user.setStatusVerseId(verseId.trim());
        user.setStatusVerseText(verseText != null ? verseText.trim() : null);
        user.setStatusCaption(caption != null ? caption.trim() : null);
        user.setStatusUpdatedAt(Instant.now());

        return appUserRepository.save(user);
    }

    @Transactional
    public AppUser clearStatusVerse(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        user.setStatusVerseId(null);
        user.setStatusBibleId(null);
        user.setStatusVerseText(null);
        user.setStatusCaption(null);
        user.setStatusUpdatedAt(null);

        return appUserRepository.save(user);
    }

}
