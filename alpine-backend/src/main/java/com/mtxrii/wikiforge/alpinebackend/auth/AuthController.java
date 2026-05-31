package com.mtxrii.wikiforge.alpinebackend.auth;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.auth.dto.AuthResponse;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.LoginRequest;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.RegisterRequest;
import com.mtxrii.wikiforge.alpinebackend.auth.dto.UserResponse;
import com.mtxrii.wikiforge.alpinebackend.common.ApiException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Map<String, Boolean>> logout() {
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/me")
    ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = parseUserId(jwt.getSubject());
        return ResponseEntity.ok(authService.getUser(userId));
    }

    private UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid access token subject");
        }
    }
}
