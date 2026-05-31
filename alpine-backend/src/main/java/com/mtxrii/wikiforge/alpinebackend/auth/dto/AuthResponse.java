package com.mtxrii.wikiforge.alpinebackend.auth.dto;

public record AuthResponse(
        UserResponse user,
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
