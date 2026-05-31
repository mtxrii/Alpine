package com.mtxrii.wikiforge.alpinebackend.activity.dto;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(UUID id, UUID boardId, UUID ticketId, UUID userId,
                               String userName, String action, String metadata, Instant createdAt) {
}
