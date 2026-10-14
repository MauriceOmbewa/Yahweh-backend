
package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 320, message = "Email must not exceed 320 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(max = 72, message = "Password must not exceed 72 characters.")
        String password

) {}
