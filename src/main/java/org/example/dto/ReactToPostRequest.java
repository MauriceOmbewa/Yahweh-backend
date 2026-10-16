package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReactToPostRequest(

        /**
         * Allowed values: AMEN, HEART, FIRE, PRAY.
         * The service validates the value; unknown types are rejected.
         */
        @NotBlank(message = "Reaction type must not be blank.")
        @Size(max = 20, message = "Reaction type must not exceed 20 characters.")
        String reactionType
) {}
