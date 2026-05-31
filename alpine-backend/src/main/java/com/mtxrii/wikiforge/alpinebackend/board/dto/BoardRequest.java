package com.mtxrii.wikiforge.alpinebackend.board.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardRequest(
        @NotNull UUID workspaceId,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @NotNull UUID createdBy
) {
}
