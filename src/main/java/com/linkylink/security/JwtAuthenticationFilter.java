package com.linkylink.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter — runs on EVERY HTTP request.
 *
 * How it works:
 *   1. Check if the request has an "Authorization: Bearer <token>" header
 *   2. If yes, validate the JWT token
 *   3. If valid, tell Spring Security "this user is authenticated"
 *   4. If no token or invalid token, do nothing (request continues as anonymous)
 *
 * OncePerRequestFilter: Guarantees this filter runs exactly once per request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Step 1: Extract the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check if it's a Bearer token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            // Step 3: Validate the token
            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);

                // Step 4: Create an Authentication object for Spring Security
                // "ROLE_" prefix is a Spring Security convention
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                var authentication = new UsernamePasswordAuthenticationToken(
                        username,       // Principal (who)
                        null,           // Credentials (not needed, token already validated)
                        authorities     // Granted authorities (what they can do)
                );

                // Step 5: Set the authentication in the SecurityContext
                // Now Spring Security knows this user is authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Step 6: Continue the filter chain (pass to the next filter or controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Skip this filter for paths that don't need authentication.
     * This is an optimization — these paths are also permitted in SecurityConfig,
     * but skipping the filter entirely is slightly faster.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || path.startsWith("/app/")
                || path.equals("/")
                || path.startsWith("/static/");
    }
}
