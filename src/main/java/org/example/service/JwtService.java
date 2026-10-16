package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Handles creation and validation of JWT access and refresh tokens.
 *
 * Algorithm: HS512
 * Access token:  short-lived  (default 15 min)
 * Refresh token: long-lived   (default 30 days)
 *
 * Token structure (claims):
 *   sub  – user UUID (string)
 *   type – "access" | "refresh"
 *   iat  – issued-at
 *   exp  – expiry
 */
@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS  = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpirySeconds;

    public JwtService(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access-token-expiry-seconds:900}") long accessTokenExpirySeconds,
            @Value("${jwt.refresh-token-expiry-seconds:2592000}") long refreshTokenExpirySeconds
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    public String generateAccessToken(UUID userId) {
        return buildToken(userId, TOKEN_TYPE_ACCESS, accessTokenExpirySeconds);
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(userId, TOKEN_TYPE_REFRESH, refreshTokenExpirySeconds);
    }

    // -------------------------------------------------------------------------
    // Token validation
    // -------------------------------------------------------------------------

    /**
     * Validates the token and returns the subject (user UUID string).
     * Throws {@link JwtException} if invalid or expired.
     */
    public String validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Token is not an access token.");
        }
        return claims.getSubject();
    }

    /**
     * Validates a refresh token and returns the subject (user UUID string).
     * Throws {@link JwtException} if invalid or expired.
     */
    public String validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Token is not a refresh token.");
        }
        return claims.getSubject();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String buildToken(UUID userId, String tokenType, long expirySeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirySeconds);

        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
