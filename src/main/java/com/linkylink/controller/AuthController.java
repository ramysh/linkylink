package com.linkylink.controller;

import com.linkylink.dto.AuthRequest;
import com.linkylink.dto.AuthResponse;
import com.linkylink.model.User;
import com.linkylink.security.JwtUtil;
import com.linkylink.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for authentication (login & registration).
 *
 * Endpoints:
 *   POST /api/auth/register — Create a new account
 *   POST /api/auth/login    — Log in and get a JWT token
 *
 * These endpoints are PUBLIC (no token required) — configured in SecurityConfig.
 *
 * @RestController = @Controller + @ResponseBody
 *   - @Controller:     Marks this as a Spring MVC controller
 *   - @ResponseBody:   Return values are serialized to JSON (not rendered as HTML)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user.
     *
     * @Valid triggers the validation annotations on AuthRequest (@NotBlank, @Size).
     * If validation fails, Spring automatically returns a 400 Bad Request.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        try {
            User user = userService.register(request.username(), request.password());
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Log in with username and password.
     * Returns a JWT token on success.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        User user = userService.authenticate(request.username(), request.password());

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }
}
