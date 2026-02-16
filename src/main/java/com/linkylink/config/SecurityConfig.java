package com.linkylink.config;

import com.linkylink.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security Configuration.
 *
 * Defines:
 *   - Which URLs are public vs. require authentication
 *   - How passwords are hashed (BCrypt)
 *   - CORS settings (so React dev server can call the API)
 *   - JWT filter integration
 *   - Stateless sessions (no server-side sessions; JWT handles auth)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * The main security filter chain.
     * Think of it as a bouncer that checks every request.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF: We're using JWT tokens, not cookies, so CSRF isn't a risk
            .csrf(csrf -> csrf.disable())

            // Enable CORS: Allow React dev server (localhost:5173) to call our API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless sessions: Don't create HTTP sessions — JWT is our session
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL authorization rules (order matters — first match wins)
            .authorizeHttpRequests(auth -> auth
                // Public: anyone can register or login
                .requestMatchers("/api/auth/**").permitAll()

                // Admin only: requires ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Authenticated: any logged-in user can access /api/** endpoints
                .requestMatchers("/api/**").authenticated()

                // Everything else is public: React static files, redirects, root
                .anyRequest().permitAll()
            )

            // Add our JWT filter BEFORE Spring's built-in username/password filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder using BCrypt.
     * BCrypt automatically generates a salt and is designed to be slow,
     * making brute-force attacks impractical.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS (Cross-Origin Resource Sharing) configuration.
     *
     * During development, React runs on http://localhost:5173 but our API
     * is on http://localhost:8080. Browsers block cross-origin requests by default.
     * This config tells the browser: "It's OK, allow requests from localhost:5173."
     *
     * In production (single JAR), CORS isn't needed since everything is same-origin.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",   // Vite dev server
                "http://localhost:3000"    // Alternate dev port
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
