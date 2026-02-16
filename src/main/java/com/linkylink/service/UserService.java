package com.linkylink.service;

import com.linkylink.model.User;
import com.linkylink.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Business logic for user management.
 *
 * Handles:
 *   - Registration (with automatic ADMIN for first user)
 *   - Authentication (password verification)
 *   - User listing and management (for admin)
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user.
     *
     * The FIRST user to register automatically becomes an ADMIN.
     * All subsequent users get the USER role.
     *
     * @throws IllegalArgumentException if username already taken
     */
    public User register(String username, String password) {
        // Check if username is already taken
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }

        // First user becomes ADMIN, everyone else is USER
        String role = userRepository.isEmpty() ? "ADMIN" : "USER";

        User user = new User(
                username,
                passwordEncoder.encode(password),  // Hash the password with BCrypt
                role,
                Instant.now().toString()            // ISO 8601 timestamp
        );

        userRepository.save(user);
        log.info("Registered new user '{}' with role '{}'", username, role);
        return user;
    }

    /**
     * Authenticate a user by verifying their password.
     *
     * @return the User if credentials are valid, null otherwise
     */
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        // BCrypt.matches() compares the raw password against the stored hash
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    /**
     * Find a user by username.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get all users (admin function).
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Update a user's role (admin function).
     */
    public User updateRole(String username, String newRole) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User '" + username + "' not found");
        }
        user.setRole(newRole);
        userRepository.save(user);
        log.info("Updated role for '{}' to '{}'", username, newRole);
        return user;
    }

    /**
     * Delete a user (admin function).
     */
    public void delete(String username) {
        userRepository.delete(username);
        log.info("Deleted user '{}'", username);
    }
}
