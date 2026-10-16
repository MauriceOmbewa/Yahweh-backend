package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank(message = "Bible ID must not be blank.")
        @Size(max = 100, message = "Bible ID must not exceed 100 characters.")
        String bibleId,

        @NotBlank(message = "Verse ID must not be blank.")
        @Size(max = 150, message = "Verse ID must not exceed 150 characters.")
        String verseId,

        @Size(max = 3000, message = "Verse text must not exceed 3000 characters.")
        String verseText,

        @Size(max = 500, message = "Caption must not exceed 500 characters.")
        String caption,

        /** PUBLIC or FRIENDS_ONLY. Defaults to FRIENDS_ONLY if omitted. */
        String visibility
) {}
