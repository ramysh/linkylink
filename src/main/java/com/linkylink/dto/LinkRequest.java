package com.linkylink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating a go link.
 */
public record LinkRequest(
        @NotBlank(message = "Keyword is required")
        @Size(min = 1, max = 50, message = "Keyword must be 1-50 characters")
        String keyword,

        @NotBlank(message = "URL is required")
        String url,

        @Size(max = 200, message = "Description must be under 200 characters")
        String description
) {
}
