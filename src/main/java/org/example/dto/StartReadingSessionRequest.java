
package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartReadingSessionRequest(

        @NotBlank(message = "Bible ID is required.")
        @Size(max = 100, message = "Bible ID cannot exceed 100 characters.")
        String bibleId

) {
}
