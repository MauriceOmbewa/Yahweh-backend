package org.example.service;

import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Manages the forgot-password / reset-password flow.
 *
 * Security model:
 *  - A 32-byte cryptographically random token is generated and returned
 *    to the caller (to be delivered out-of-band, e.g. via email).
 *  - Only the SHA-256 hash of the token is stored in the database, so a
 *    DB breach does not expose usable tokens.
 *  - Tokens expire after a configurable window (default 1 hour).
 *  - The token is single-use: cleared immediately on successful reset.
 *
 * NOTE: This service does not send emails — that integration point is
 * left to a future EmailService. The raw token is returned from
 * initiateReset() so the caller (controller / email service) can deliver it.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${auth.password-reset-expiry-seconds:3600}")
    private long expirySeconds;

    public PasswordResetService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -------------------------------------------------------------------------
    // Initiate reset
    // -------------------------------------------------------------------------

    /**
     * Generates a reset token for the given email address.
     *
     * Returns the raw (un-hashed) token if the email is found, or
     * {@link Optional#empty()} if no account exists for that email.
     *
     * The caller should always return a generic 200 response to the client
     * regardless of whether the email was found, to avoid user enumeration.
     */
    @Transactional
    public Optional<String> initiateReset(String email) {
        Optional<AppUser> userOpt = appUserRepository
                .findByEmail(email.trim().toLowerCase());

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        AppUser user = userOpt.get();

        // Generate a URL-safe 32-byte random token.
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // Store only the hash.
        user.setPasswordResetTokenHash(sha256Hex(rawToken));
        user.setPasswordResetTokenExpiresAt(Instant.now().plusSeconds(expirySeconds));
        appUserRepository.save(user);

        log.info("Password reset token generated for userId={}", user.getId());
        return Optional.of(rawToken);
    }

    // -------------------------------------------------------------------------
    // Complete reset
    // -------------------------------------------------------------------------

    /**
     * Validates the token and sets the new password.
     * Throws {@link IllegalArgumentException} if the token is invalid or expired.
     */
    @Transactional
    public void completeReset(String rawToken, String newPassword) {
        String tokenHash = sha256Hex(rawToken);

        AppUser user = appUserRepository.findByPasswordResetTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired password reset token."));

        if (user.getPasswordResetTokenExpiresAt() == null
                || Instant.now().isAfter(user.getPasswordResetTokenExpiresAt())) {
            // Clear the expired token for hygiene.
            clearToken(user);
            throw new IllegalArgumentException(
                    "Invalid or expired password reset token.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        clearToken(user);
        appUserRepository.save(user);

        log.info("Password reset completed for userId={}", user.getId());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void clearToken(AppUser user) {
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetTokenExpiresAt(null);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
