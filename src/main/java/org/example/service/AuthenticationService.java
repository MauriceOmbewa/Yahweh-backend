package org.example.service;

import io.jsonwebtoken.JwtException;
import org.example.dto.AuthTokenResponse;
import org.example.model.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Stateless JWT-based authentication.
 * Replaces the old session-based implementation.
 */
@Service
public class AuthenticationService {

    private final AppUserService appUserService;
    private final JwtService jwtService;

    @Value("${jwt.access-token-expiry-seconds:900}")
    private long accessTokenExpirySeconds;

    public AuthenticationService(
            AppUserService appUserService,
            JwtService jwtService
    ) {
        this.appUserService = appUserService;
        this.jwtService = jwtService;
    }

    /**
     * Validates credentials and returns a token pair.
     */
    public AuthTokenResponse login(String email, String password) {
        AppUser user = appUserService.authenticate(email, password);
        return issueTokens(user);
    }

    /**
     * Issues a new token pair from a valid refresh token.
     * Throws IllegalArgumentException if the refresh token is invalid/expired.
     */
    public AuthTokenResponse refresh(String refreshToken) {
        String userIdStr;
        try {
            userIdStr = jwtService.validateRefreshToken(refreshToken);
        } catch (JwtException e) {
            throw new IllegalArgumentException(
                    "Invalid or expired refresh token. Please log in again.");
        }

        AppUser user = appUserService.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return issueTokens(user);
    }

    /**
     * Builds an AuthTokenResponse for the given user.
     * Shared by login, register, Google sign-in, and token refresh.
     */
    public AuthTokenResponse issueTokens(AppUser user) {
        String accessToken  = jwtService.generateAccessToken(user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        return AuthTokenResponse.of(accessToken, refreshToken, accessTokenExpirySeconds, user);
    }
}
