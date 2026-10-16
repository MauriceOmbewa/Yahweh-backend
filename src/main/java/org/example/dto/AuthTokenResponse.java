package org.example.dto;

import org.example.model.AppUser;

/**
 * Returned on login, register, token refresh, and Google Sign-In.
 * The mobile / web client stores the tokens and sends the access token
 * as a Bearer header on every subsequent request.
 */
public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,       // always "Bearer"
        long expiresIn,         // access token TTL in seconds
        UserResponse user
) {
    public static AuthTokenResponse of(
            String accessToken,
            String refreshToken,
            long expiresIn,
            AppUser user
    ) {
        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                UserResponse.from(user)
        );
    }
}
