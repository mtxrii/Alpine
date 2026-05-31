package com.mtxrii.wikiforge.alpinebackend.board.dto;

import jakarta.validation.constraints.Size;

public record BoardPatchRequest(
        @Size(max = 160) String name,
        @Size(max = 2000) String description
) {
}
