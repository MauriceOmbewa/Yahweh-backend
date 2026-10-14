
package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.RegisterRequest;
import org.example.dto.UserResponse;
import org.example.model.AppUser;
import org.example.service.AppUserService;
import org.example.dto.LoginRequest;
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

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
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
            @Valid @RequestBody LoginRequest request) {

        try {
            AppUser user = appUserService.authenticate(
                    request.email(),
                    request.password()
            );

            return ResponseEntity.ok(UserResponse.from(user));

        } catch (BadCredentialsException exception) {
            throw new BadCredentialsException("Invalid email or password.");
        }
    }
}
