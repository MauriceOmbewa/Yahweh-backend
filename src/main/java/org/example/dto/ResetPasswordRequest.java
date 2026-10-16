package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(message = "Reset token must not be blank.")
        String token,

        @NotBlank(message = "New password must not be blank.")
        @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters.")
        String newPassword
) {}
