package com.mtxrii.wikiforge.alpinebackend.board.dto;

import java.time.Instant;
import java.util.UUID;

public record BoardResponse(UUID id, UUID workspaceId, String name, String description, UUID createdBy,
                            Instant createdAt, Instant updatedAt) {
}
