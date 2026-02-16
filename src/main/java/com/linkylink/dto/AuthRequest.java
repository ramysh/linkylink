package com.linkylink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) for login and registration requests.
 *
 * Java Records: A concise way to create immutable data classes.
 * The compiler auto-generates: constructor, getters, equals(), hashCode(), toString().
 *
 * Validation annotations:
 *   @NotBlank — must not be null or empty
 *   @Size     — must be within the specified length range
 */
public record AuthRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password
) {
}
