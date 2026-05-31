package com.mtxrii.wikiforge.alpinebackend.workspace.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull UUID ownerId
) {
}
