
package org.example.service;

import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Optional<AppUser> findById(UUID id) {
        return appUserRepository.findById(id);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return appUserRepository.existsByEmail(email);
    }

    @Transactional
    public AppUser createUser(
            String email,
            String displayName,
            String passwordHash
    ) {
        String normalizedEmail = email.trim().toLowerCase();

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(displayName.trim());
        user.setPasswordHash(passwordHash);

        return appUserRepository.save(user);
    }
}
