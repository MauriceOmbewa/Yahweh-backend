package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.dto.AuthTokenResponse;
import org.example.dto.GoogleSignInRequest;
import org.example.dto.LoginRequest;
import org.example.dto.RefreshTokenRequest;
import org.example.dto.RegisterRequest;
import org.example.dto.UserResponse;
import org.example.model.AppUser;
import org.example.service.AppUserService;
import org.example.service.AuthenticationService;
import org.example.service.GoogleAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Auth", description = "Registration, login, token refresh, and profile")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final AuthenticationService authenticationService;
    private final GoogleAuthService googleAuthService;

    public AuthController(
            AppUserService appUserService,
            AuthenticationService authenticationService,
            GoogleAuthService googleAuthService
    ) {
        this.appUserService = appUserService;
        this.authenticationService = authenticationService;
        this.googleAuthService = googleAuthService;
    }

    @Operation(summary = "Register a new account",
               description = "Creates a new user and returns a JWT token pair immediately.")
    @PostMapping("/register")
    public ResponseEntity<AuthTokenResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AppUser user = appUserService.createUser(
                request.email(), request.displayName(), request.password());
        AuthTokenResponse tokens = authenticationService.issueTokens(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @Operation(summary = "Login with email and password",
               description = "Returns a JWT access token (15 min) and refresh token (30 days).")
    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokenResponse tokens = authenticationService.login(
                request.email(), request.password());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Sign in with Google",
               description = "Verifies a Google ID token issued by the client SDK. "
                       + "Creates an account automatically if none exists for this Google account.")
    @PostMapping("/google")
    public ResponseEntity<AuthTokenResponse> googleSignIn(
            @Valid @RequestBody GoogleSignInRequest request
    ) {
        AuthTokenResponse tokens = googleAuthService.signIn(request.idToken());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Refresh access token",
               description = "Exchange a valid refresh token for a new access + refresh token pair.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthTokenResponse tokens = authenticationService.refresh(request.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Get current user profile",
               security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return appUserService.findById(userId)
                .map(user -> ResponseEntity.ok(UserResponse.from(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Logout (client-side)",
               description = "JWT is stateless — logout is handled client-side by discarding tokens. "
                       + "This endpoint exists for API symmetry and returns 204.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
