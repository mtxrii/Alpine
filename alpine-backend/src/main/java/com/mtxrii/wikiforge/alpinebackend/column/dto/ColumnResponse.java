package com.mtxrii.wikiforge.alpinebackend.column.dto;

import java.time.Instant;
import java.util.UUID;

public record ColumnResponse(UUID id, UUID boardId, String name, Integer position, String color,
                             Instant createdAt, Instant updatedAt) {
}
