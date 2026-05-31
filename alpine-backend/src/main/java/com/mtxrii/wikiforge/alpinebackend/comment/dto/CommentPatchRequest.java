package com.mtxrii.wikiforge.alpinebackend.comment.dto;

import jakarta.validation.constraints.Size;

public record CommentPatchRequest(
        @Size(max = 5000) String body
) {
}
