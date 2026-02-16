package com.linkylink.controller;

import com.linkylink.model.Link;
import com.linkylink.service.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

/**
 * The HEART of the Go Links app — handles keyword redirects.
 *
 * When someone types "go/google" in their browser:
 *   1. The browser sends: GET /google
 *   2. This controller receives it
 *   3. Looks up "google" in DynamoDB
 *   4. Returns a 302 redirect to https://www.google.com
 *   5. The browser follows the redirect → user lands on Google
 *
 * Note: This uses @Controller (not @RestController) because we're returning
 * HTTP redirects, not JSON responses.
 */
@Controller
public class RedirectController {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final LinkService linkService;

    public RedirectController(LinkService linkService) {
        this.linkService = linkService;
    }

    /**
     * Root path: redirect to the management UI.
     */
    @GetMapping("/")
    public ResponseEntity<Void> root() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/app/"))
                .build();
    }

    /**
     * Resolve a go link keyword and redirect.
     *
     * {keyword} is a path variable — Spring extracts it from the URL.
     * Example: GET /google → keyword = "google"
     *
     * 302 Found: A temporary redirect. The browser will always come back
     * to ask the go link server, so we can track clicks and update URLs.
     * (301 would be cached by the browser, which we don't want.)
     */
    @GetMapping("/{keyword}")
    public ResponseEntity<Void> redirect(@PathVariable String keyword) {
        Link link = linkService.resolve(keyword);

        if (link != null) {
            log.info("Redirecting go/{} → {}", keyword, link.getUrl());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(link.getUrl()))
                    .build();
        }

        // Keyword not found — redirect to the app with the keyword as a search hint
        log.debug("Go link '{}' not found, redirecting to app", keyword);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/app/?notfound=" + keyword))
                .build();
    }
}
