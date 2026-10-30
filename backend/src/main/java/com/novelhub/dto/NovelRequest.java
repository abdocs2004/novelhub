package com.novelhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovelRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title is too long")
        String title,

        @NotBlank(message = "Author is required")
        @Size(max = 150, message = "Author is too long")
        String author,

        @Size(max = 5000, message = "Description is too long")
        String description,

        @Size(max = 500, message = "Cover image URL is too long")
        String coverImage
) {
}
