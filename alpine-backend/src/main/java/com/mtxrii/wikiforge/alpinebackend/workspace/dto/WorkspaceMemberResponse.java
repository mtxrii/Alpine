package com.mtxrii.wikiforge.alpinebackend.workspace.dto;

import java.util.UUID;

public record WorkspaceMemberResponse(
        UUID id,
        String name,
        String email,
        String role
) {
}
