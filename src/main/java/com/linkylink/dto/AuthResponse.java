package com.linkylink.dto;

/**
 * DTO returned after successful login/registration.
 * Contains the JWT token the client will use for subsequent API calls.
 */
public record AuthResponse(
        String token,
        String username,
        String role
) {
}
