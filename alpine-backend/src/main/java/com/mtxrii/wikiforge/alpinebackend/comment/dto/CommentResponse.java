package com.mtxrii.wikiforge.alpinebackend.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(UUID id, UUID ticketId, UUID authorId, String authorName,
                              String body, Instant createdAt, Instant updatedAt) {
}
