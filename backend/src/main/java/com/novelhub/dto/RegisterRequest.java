package com.novelhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Records are immutable data carriers: perfect for DTOs. The annotations are checked when the
// controller parameter is annotated with @Valid.
public record RegisterRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name is too long")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        @Size(max = 150, message = "Email is too long")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters") // BCrypt ignores bytes after 72
        String password
) {
}
