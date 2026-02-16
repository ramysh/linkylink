package com.linkylink.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for creating and validating JWT (JSON Web Tokens).
 *
 * How JWT auth works:
 *   1. User logs in with username/password
 *   2. Server verifies credentials, creates a JWT containing user info
 *   3. Client stores the JWT (in localStorage) and sends it with every request
 *   4. Server validates the JWT on each request — no session needed (stateless!)
 *
 * A JWT has three parts: HEADER.PAYLOAD.SIGNATURE
 *   - Header:    Algorithm used (HS256)
 *   - Payload:   User data (username, role, expiration)
 *   - Signature: Ensures the token wasn't tampered with
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:}")
    private String configuredSecret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey signingKey;

    /**
     * @PostConstruct runs after Spring creates this bean and injects the values.
     * We use it to initialize the signing key.
     */
    @PostConstruct
    public void init() {
        if (configuredSecret != null && configuredSecret.length() >= 32) {
            // Use the configured secret (must be at least 256 bits = 32 bytes for HS256)
            signingKey = Keys.hmacShaKeyFor(configuredSecret.getBytes(StandardCharsets.UTF_8));
            log.info("JWT signing key loaded from configuration.");
        } else {
            // Generate a random key — tokens won't survive app restarts
            signingKey = Jwts.SIG.HS256.key().build();
            log.warn("No JWT_SECRET configured — using random key. Tokens will NOT survive restarts!");
            log.warn("Set JWT_SECRET env variable (min 32 chars) for persistent tokens.");
        }
    }

    /**
     * Generate a JWT token for an authenticated user.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)                        // Who this token is for
                .claim("role", role)                      // Custom claim: user's role
                .issuedAt(new Date())                     // When the token was created
                .expiration(new Date(System.currentTimeMillis() + expiration)) // When it expires
                .signWith(signingKey)                     // Sign with our secret key
                .compact();                               // Build the token string
    }

    /**
     * Extract the username from a JWT token.
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extract the role from a JWT token.
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Validate a JWT token: checks signature and expiration.
     */
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse and validate the token, returning its claims (payload data).
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)    // Verify signature with our key
                .build()
                .parseSignedClaims(token)  // Parse and validate
                .getPayload();             // Get the payload (claims)
    }
}
