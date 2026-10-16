package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.ForgotPasswordRequest;
import org.example.dto.ResetPasswordRequest;
import org.example.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@Tag(name = "Auth", description = "Registration, login, token refresh, and profile")
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Operation(
            summary = "Request a password reset",
            description = "Generates a single-use reset token for the given email address. "
                    + "Always returns 200 regardless of whether the email exists "
                    + "(prevents user enumeration). "
                    + "In production the token is delivered via email; "
                    + "the raw token is logged at INFO level for development purposes only."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        Optional<String> rawToken = passwordResetService.initiateReset(request.email());

        // Log token in dev — replace with EmailService call in production.
        rawToken.ifPresent(token ->
                log.info("DEV — password reset token for {}: {}", request.email(), token));

        // Always return the same response to avoid leaking whether the email exists.
        return ResponseEntity.ok(Map.of(
                "message",
                "If an account exists for that email, a password reset link has been sent."
        ));
    }

    @Operation(
            summary = "Reset password with token",
            description = "Validates the reset token and sets a new password. "
                    + "Tokens are single-use and expire after 1 hour."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.completeReset(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully. Please log in with your new password."
        ));
    }
}
