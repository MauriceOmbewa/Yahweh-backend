
package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.RegisterRequest;
import org.example.dto.UserResponse;
import org.example.model.AppUser;
import org.example.service.AppUserService;
import org.example.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.example.service.AuthenticationService;
import org.springframework.security.core.Authentication;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final AuthenticationService authenticationService;

    public AuthController(
            AppUserService appUserService,
            AuthenticationService authenticationService) {
        this.appUserService = appUserService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AppUser user = appUserService.createUser(
                request.email(),
                request.displayName(),
                request.password()
        );

        UserResponse response = UserResponse.from(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AppUser user = authenticationService.login(
                request.email(),
                request.password(),
                httpRequest
        );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());

        return appUserService.findById(userId)
                .map(user -> ResponseEntity.ok(UserResponse.from(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        authenticationService.logout(request, response);

        return ResponseEntity.noContent().build();
    }

}
