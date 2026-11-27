package com.university.socialnetwork.security;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("test-security-endpoints")
@RestController
public class TestSecurityEndpointsController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/feed")
    public String feed() {
        return "feed";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin-users";
    }

    @PostMapping("/api/public-ping")
    public String publicPing() {
        return "ok";
    }
}