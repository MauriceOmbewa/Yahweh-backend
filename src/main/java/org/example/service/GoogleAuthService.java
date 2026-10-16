package org.example.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.example.dto.AuthTokenResponse;
import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Verifies a Google ID token supplied by the mobile/web client and either
 * signs in the matching account or creates a new one (auto-registration).
 *
 * Flow:
 *  1. Client signs in with Google and receives an ID token.
 *  2. Client sends the ID token to POST /api/auth/google.
 *  3. This service verifies the token with Google's public keys.
 *  4. If a user with that google_id exists → sign them in.
 *  5. If a user with that email exists but no google_id → link the account.
 *  6. Otherwise → create a new account (password_hash stays null).
 *  7. Return a JWT token pair.
 */
@Service
public class GoogleAuthService {

    private final AppUserRepository appUserRepository;
    private final AuthenticationService authenticationService;
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(
            AppUserRepository appUserRepository,
            AuthenticationService authenticationService,
            @Value("${google.client-id:}") String googleClientId
    ) {
        this.appUserRepository = appUserRepository;
        this.authenticationService = authenticationService;

        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Transactional
    public AuthTokenResponse signIn(String idTokenString) {
        GoogleIdToken idToken = verifyToken(idTokenString);
        GoogleIdToken.Payload payload = idToken.getPayload();

        String googleId   = payload.getSubject();
        String email      = payload.getEmail().toLowerCase();
        String name       = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 1. Try to find by google_id first (fastest path).
        Optional<AppUser> byGoogleId = appUserRepository.findByGoogleId(googleId);
        if (byGoogleId.isPresent()) {
            return authenticationService.issueTokens(byGoogleId.get());
        }

        // 2. Try to find by email and link the Google account.
        Optional<AppUser> byEmail = appUserRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            AppUser existing = byEmail.get();
            existing.setGoogleId(googleId);
            // Update avatar only if the user hasn't set their own.
            if (existing.getAvatarUrl() == null && pictureUrl != null) {
                existing.setAvatarUrl(pictureUrl);
            }
            appUserRepository.save(existing);
            return authenticationService.issueTokens(existing);
        }

        // 3. Create a brand-new account.
        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setDisplayName(name != null ? name : email);
        newUser.setGoogleId(googleId);
        newUser.setAvatarUrl(pictureUrl);
        // password_hash stays null — Google-only accounts have no password.

        AppUser saved = appUserRepository.save(newUser);
        return authenticationService.issueTokens(saved);
    }

    // -------------------------------------------------------------------------

    private GoogleIdToken verifyToken(String idTokenString) {
        try {
            GoogleIdToken token = verifier.verify(idTokenString);
            if (token == null) {
                throw new IllegalArgumentException("Google ID token is invalid or has expired.");
            }
            return token;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not verify Google ID token: " + e.getMessage());
        }
    }
}
