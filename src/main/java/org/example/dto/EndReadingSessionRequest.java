
package org.example.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EndReadingSessionRequest(

        @NotNull(message = "Session ID is required.")
        UUID sessionId

) {
}
