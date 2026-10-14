
package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 320, message = "Email must not exceed 320 characters.")
        String email,

        @NotBlank(message = "Display name is required.")
        @Size(max = 100, message = "Display name must not exceed 100 characters.")
        String displayName,

        @NotBlank(message = "Password is required.")
        @Size(min = 12, max = 72,
                message = "Password must contain between 12 and 72 characters.")
        String password

) {
}
