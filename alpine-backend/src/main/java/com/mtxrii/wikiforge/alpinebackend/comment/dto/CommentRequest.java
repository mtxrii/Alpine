package com.mtxrii.wikiforge.alpinebackend.comment.dto;

import java.util.UUID;

public record CommentRequest(UUID authorId, String body) {
}
