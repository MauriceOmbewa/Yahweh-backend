
package org.example.service;

import org.example.exception.EmailAlreadyExistsException;
import org.example.model.AppUser;
import org.example.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode("StrongPassword123"))
                .thenReturn("hashed-password");
    }

    @Test
    void createUserShouldSaveUserWithHashedPassword() {
        when(appUserRepository.existsByEmail("user@example.com"))
                .thenReturn(false);

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUser user = appUserService.createUser(
                "user@example.com",
                "Test User",
                "StrongPassword123"
        );

        assertEquals("user@example.com", user.getEmail());
        assertEquals("Test User", user.getDisplayName());
        assertEquals("hashed-password", user.getPasswordHash());
        assertNotEquals("StrongPassword123", user.getPasswordHash());

        verify(appUserRepository).save(any(AppUser.class));
        verify(passwordEncoder).encode("StrongPassword123");
    }

    @Test
    void findByEmailShouldNormalizeEmail() {
        AppUser existingUser = new AppUser();

        when(appUserRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(existingUser));

        Optional<AppUser> result =
                appUserService.findByEmail(" USER@EXAMPLE.COM ");

        assertTrue(result.isPresent());

        verify(appUserRepository).findByEmail("user@example.com");
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        when(appUserRepository.existsByEmail("user@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> appUserService.createUser(
                        "user@example.com",
                        "Another User",
                        "StrongPassword123"
                )
        );

        verify(appUserRepository, never()).save(any(AppUser.class));
    }
}
