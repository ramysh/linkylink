package com.linkylink.controller;

import com.linkylink.dto.LinkRequest;
import com.linkylink.model.Link;
import com.linkylink.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for go link CRUD operations.
 *
 * Endpoints (all require authentication):
 *   GET    /api/links        — List the current user's go links
 *   GET    /api/links/all    — List ALL go links (public directory)
 *   POST   /api/links        — Create a new go link
 *   PUT    /api/links/{keyword} — Update a go link
 *   DELETE /api/links/{keyword} — Delete a go link
 *
 * The 'Authentication' parameter is automatically injected by Spring Security.
 * It contains the current user's info (extracted from the JWT token by our filter).
 */
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    /**
     * List the current user's go links.
     */
    @GetMapping
    public ResponseEntity<List<Link>> getMyLinks(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(linkService.findByOwner(username));
    }

    /**
     * List ALL go links (public directory).
     */
    @GetMapping("/all")
    public ResponseEntity<List<Link>> getAllLinks() {
        return ResponseEntity.ok(linkService.findAll());
    }

    /**
     * Create a new go link.
     */
    @PostMapping
    public ResponseEntity<?> createLink(@Valid @RequestBody LinkRequest request,
                                        Authentication auth) {
        try {
            String username = auth.getName();
            Link link = linkService.create(
                    request.keyword(), request.url(), request.description(), username);
            return ResponseEntity.ok(link);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update an existing go link.
     */
    @PutMapping("/{keyword}")
    public ResponseEntity<?> updateLink(@PathVariable String keyword,
                                        @Valid @RequestBody LinkRequest request,
                                        Authentication auth) {
        try {
            String username = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            Link link = linkService.update(keyword, request.url(), request.description(),
                    username, isAdmin);
            return ResponseEntity.ok(link);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete a go link.
     */
    @DeleteMapping("/{keyword}")
    public ResponseEntity<?> deleteLink(@PathVariable String keyword,
                                        Authentication auth) {
        try {
            String username = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            linkService.delete(keyword, username, isAdmin);
            return ResponseEntity.ok(Map.of("message", "Deleted go/" + keyword));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
