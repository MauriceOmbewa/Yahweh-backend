
package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookmarkCollectionRequest(

        @NotBlank(message = "Collection name is required.")
        @Size(max = 100, message = "Collection name must not exceed 100 characters.")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters.")
        String description

) {
}
