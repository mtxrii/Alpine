package com.mtxrii.wikiforge.alpinebackend.label.dto;

import jakarta.validation.constraints.Size;

public record LabelPatchRequest(
        @Size(max = 80) String name,
        @Size(max = 64) String color
) {
}
