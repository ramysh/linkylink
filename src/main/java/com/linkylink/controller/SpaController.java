package com.linkylink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA (Single Page Application) Forwarding Controller.
 *
 * React is a SPA — it handles routing on the client side.
 * When a user navigates to /app/dashboard, the browser requests that path from the server.
 * But there's no server-side route for /app/dashboard — it's a React route!
 *
 * This controller catches all /app/** requests and forwards them to index.html,
 * where React Router takes over and renders the correct page.
 *
 * Note: Static files (JS, CSS, images) in /app/assets/ are served directly by
 * Spring Boot's built-in static resource handling, not this controller.
 */
@Controller
public class SpaController {

    @GetMapping(value = {
            "/app",
            "/app/",
            "/app/login",
            "/app/register",
            "/app/dashboard",
            "/app/admin"
    })
    public String forwardToReact() {
        // "forward:" keeps the URL in the browser unchanged
        // (unlike "redirect:" which changes the URL)
        return "forward:/app/index.html";
    }
}
