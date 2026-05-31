package com.mtxrii.wikiforge.alpinebackend.comment.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotNull UUID authorId,
        @NotBlank @Size(max = 5000) String body
) {
}
