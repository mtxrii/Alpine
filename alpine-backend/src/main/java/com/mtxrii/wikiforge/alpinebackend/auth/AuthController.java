package com.mtxrii.wikiforge.alpinebackend.auth;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Map<String, Boolean>> logout() {
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/me")
    ResponseEntity<UserResponse> me(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(authService.getUser(userId));
    }

    public record RegisterRequest(String email, String name, String avatarUrl) {
    }

    public record LoginRequest(String email) {
    }

    public record UserResponse(UUID id, String email, String name, String avatarUrl) {
    }
}
