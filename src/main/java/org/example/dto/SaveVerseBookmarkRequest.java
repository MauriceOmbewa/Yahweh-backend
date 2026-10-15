
package org.example.dto;

import jakarta.validation.constraints.Size;

public record SaveVerseBookmarkRequest(

        @Size(max = 20, message = "Highlight color must not exceed 20 characters.")
        String highlightColor,

        @Size(max = 10000, message = "Note must not exceed 10000 characters.")
        String note

) {
}
