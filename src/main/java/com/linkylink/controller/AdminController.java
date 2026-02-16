package com.linkylink.controller;

import com.linkylink.model.Link;
import com.linkylink.model.User;
import com.linkylink.service.LinkService;
import com.linkylink.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for admin-only operations.
 *
 * All endpoints require ADMIN role — enforced in SecurityConfig:
 *   .requestMatchers("/api/admin/**").hasRole("ADMIN")
 *
 * Endpoints:
 *   GET    /api/admin/users               — List all users
 *   PUT    /api/admin/users/{username}/role — Change a user's role
 *   DELETE /api/admin/users/{username}     — Delete a user
 *   GET    /api/admin/links               — List ALL go links
 *   DELETE /api/admin/links/{keyword}     — Delete any go link
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final LinkService linkService;

    public AdminController(UserService userService, LinkService linkService) {
        this.userService = userService;
        this.linkService = linkService;
    }

    // ==================== User Management ====================

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, String>>> listUsers() {
        // Return users WITHOUT password hashes (security best practice)
        List<Map<String, String>> users = userService.findAll().stream()
                .map(user -> Map.of(
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "createdAt", user.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{username}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String username,
                                            @RequestBody Map<String, String> body) {
        try {
            String newRole = body.get("role");
            if (!"USER".equals(newRole) && !"ADMIN".equals(newRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role must be USER or ADMIN"));
            }
            User user = userService.updateRole(username, newRole);
            return ResponseEntity.ok(Map.of(
                    "username", user.getUsername(),
                    "role", user.getRole()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userService.delete(username);
        return ResponseEntity.ok(Map.of("message", "Deleted user '" + username + "'"));
    }

    // ==================== Link Management ====================

    @GetMapping("/links")
    public ResponseEntity<List<Link>> listAllLinks() {
        return ResponseEntity.ok(linkService.findAll());
    }

    @DeleteMapping("/links/{keyword}")
    public ResponseEntity<?> deleteLink(@PathVariable String keyword) {
        try {
            // Admin can delete any link — pass isAdmin=true
            linkService.delete(keyword, "admin", true);
            return ResponseEntity.ok(Map.of("message", "Deleted go/" + keyword));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
