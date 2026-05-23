package com.mtxrii.wikiforge.alpinebackend.common;

import java.time.Instant;
import java.util.UUID;

public final class SimpleDtos {
    private SimpleDtos() {
    }

    public record UserSummary(UUID id, String name) {
    }

    public record ColumnSummary(UUID id, String name) {
    }

    public record BoardResponse(UUID id, UUID workspaceId, String name, String description, UUID createdBy,
                                Instant createdAt, Instant updatedAt) {
    }

    public record ColumnResponse(UUID id, UUID boardId, String name, Integer position, String color,
                                 Instant createdAt, Instant updatedAt) {
    }

    public record LabelResponse(UUID id, UUID boardId, String name, String color) {
    }
}
