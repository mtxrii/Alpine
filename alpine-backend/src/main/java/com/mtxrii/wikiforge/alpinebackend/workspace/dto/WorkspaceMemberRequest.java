package com.mtxrii.wikiforge.alpinebackend.workspace.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record WorkspaceMemberRequest(
        @NotNull UUID userId,
        String role
) {
}
