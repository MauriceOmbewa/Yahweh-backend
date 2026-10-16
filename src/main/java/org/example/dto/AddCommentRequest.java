package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(

        @NotBlank(message = "Comment body must not be blank.")
        @Size(max = 1000, message = "Comment must not exceed 1000 characters.")
        String body
) {}
