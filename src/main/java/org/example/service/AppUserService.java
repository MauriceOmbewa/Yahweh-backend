
package org.example.service;

import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.example.exception.EmailAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AppUser> findById(UUID id) {
        return appUserRepository.findById(id);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(normalizeEmail(email));
    }

    public boolean emailExists(String email) {
        return appUserRepository.existsByEmail(normalizeEmail(email));
    }

    @Transactional
    public AppUser createUser(
            String email,
            String displayName,
            String rawPassword
    ) {
        String normalizedEmail = normalizeEmail(email);

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists."
            );
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(displayName.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return appUserRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public AppUser authenticate(String email, String rawPassword) {
        AppUser user = findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
                        "Invalid email or password."));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid email or password.");
        }

        return user;
    }

}
