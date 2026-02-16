package com.linkylink.service;

import com.linkylink.model.Link;
import com.linkylink.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Business logic for go link management.
 *
 * Handles:
 *   - CRUD operations on go links
 *   - Keyword validation (reserved words)
 *   - Ownership checks
 *   - Click tracking
 */
@Service
public class LinkService {

    private static final Logger log = LoggerFactory.getLogger(LinkService.class);

    // Keywords that can't be used as go links (they'd conflict with app routes)
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "api", "app", "static", "favicon.ico", "health"
    );

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /**
     * Create a new go link.
     *
     * @throws IllegalArgumentException if keyword is reserved or already taken
     */
    public Link create(String keyword, String url, String description, String ownerUsername) {
        // Normalize keyword to lowercase
        keyword = keyword.toLowerCase().trim();

        // Validate keyword
        if (RESERVED_KEYWORDS.contains(keyword)) {
            throw new IllegalArgumentException("'" + keyword + "' is a reserved keyword");
        }
        if (keyword.isEmpty() || keyword.length() > 50) {
            throw new IllegalArgumentException("Keyword must be 1-50 characters");
        }
        if (!keyword.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Keyword can only contain lowercase letters, numbers, and hyphens");
        }

        // Check if keyword already exists
        if (linkRepository.findByKeyword(keyword) != null) {
            throw new IllegalArgumentException("Keyword '" + keyword + "' is already taken");
        }

        // Ensure URL has a protocol
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        Link link = new Link(keyword, url, ownerUsername, description, Instant.now().toString());
        linkRepository.save(link);
        log.info("Created go link: go/{} → {} (owner: {})", keyword, url, ownerUsername);
        return link;
    }

    /**
     * Update an existing go link.
     * Only the owner or an admin can update.
     */
    public Link update(String keyword, String newUrl, String newDescription,
                         String requestingUsername, boolean isAdmin) {
        Link existing = linkRepository.findByKeyword(keyword);
        if (existing == null) {
            throw new IllegalArgumentException("Go link '" + keyword + "' not found");
        }

        // Ownership check: only owner or admin can update
        if (!isAdmin && !existing.getOwnerUsername().equals(requestingUsername)) {
            throw new SecurityException("You can only edit your own go links");
        }

        // Ensure URL has a protocol
        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            newUrl = "https://" + newUrl;
        }

        existing.setUrl(newUrl);
        existing.setDescription(newDescription);
        linkRepository.save(existing);
        log.info("Updated go link: go/{} → {}", keyword, newUrl);
        return existing;
    }

    /**
     * Delete a go link.
     * Only the owner or an admin can delete.
     */
    public void delete(String keyword, String requestingUsername, boolean isAdmin) {
        Link existing = linkRepository.findByKeyword(keyword);
        if (existing == null) {
            throw new IllegalArgumentException("Go link '" + keyword + "' not found");
        }

        if (!isAdmin && !existing.getOwnerUsername().equals(requestingUsername)) {
            throw new SecurityException("You can only delete your own go links");
        }

        linkRepository.delete(keyword);
        log.info("Deleted go link: go/{} (by: {})", keyword, requestingUsername);
    }

    /**
     * Resolve a keyword to its URL and increment the click counter.
     * This is the core function: what happens when someone types "go/keyword".
     *
     * @return the Link, or null if not found
     */
    public Link resolve(String keyword) {
        Link link = linkRepository.findByKeyword(keyword.toLowerCase().trim());
        if (link != null) {
            // Increment click count asynchronously-ish (atomic DynamoDB update)
            linkRepository.incrementClickCount(link.getKeyword());
        }
        return link;
    }

    /**
     * Find a go link by keyword (without incrementing click count).
     */
    public Link findByKeyword(String keyword) {
        return linkRepository.findByKeyword(keyword.toLowerCase().trim());
    }

    /**
     * Get all go links owned by a specific user.
     */
    public List<Link> findByOwner(String username) {
        return linkRepository.findByOwner(username);
    }

    /**
     * Get ALL go links (for admin or public listing).
     */
    public List<Link> findAll() {
        return linkRepository.findAll();
    }
}
