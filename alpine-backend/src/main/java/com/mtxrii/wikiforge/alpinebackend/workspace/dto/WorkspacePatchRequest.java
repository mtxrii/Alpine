package com.mtxrii.wikiforge.alpinebackend.workspace.dto;

import jakarta.validation.constraints.Size;

public record WorkspacePatchRequest(
        @Size(max = 120) String name
) {
}
