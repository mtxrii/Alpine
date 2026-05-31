package com.mtxrii.wikiforge.alpinebackend.auth.dto;

import java.util.UUID;

public record UserResponse(UUID id, String email, String name, String avatarUrl) {
}
