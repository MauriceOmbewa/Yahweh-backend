package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTimezoneRequest(

        @NotBlank(message = "Timezone must not be blank.")
        @Size(max = 64, message = "Timezone must not exceed 64 characters.")
        String timezone
) {
}
