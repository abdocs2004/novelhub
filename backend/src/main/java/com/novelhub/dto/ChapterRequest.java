package com.novelhub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ChapterRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title is too long")
        String title,

        @NotBlank(message = "Content is required")
        String content,

        // 0 = free chapter
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimals")
        BigDecimal price,

        @NotNull(message = "Chapter number is required")
        @Min(value = 1, message = "Chapter number starts at 1")
        Integer chapterNumber
) {
}
