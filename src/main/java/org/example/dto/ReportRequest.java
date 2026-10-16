package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportRequest(

        /**
         * Must match a ReportReason enum value:
         * SPAM, INAPPROPRIATE, HARASSMENT, HATE_SPEECH, MISINFORMATION, OTHER
         */
        @NotBlank(message = "Reason must not be blank.")
        String reason,

        @Size(max = 1000, message = "Description must not exceed 1000 characters.")
        String description
) {}
