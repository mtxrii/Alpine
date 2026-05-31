package com.mtxrii.wikiforge.alpinebackend.workspace.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceResponse(UUID id, String name, UUID ownerId, Instant createdAt, Instant updatedAt) {
}
