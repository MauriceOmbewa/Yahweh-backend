package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(
        /**
         * The ID token returned by the Google Sign-In SDK on the client.
         * The backend verifies this token against Google's public keys.
         */
        @NotBlank(message = "Google ID token must not be blank.")
        String idToken
) {}
