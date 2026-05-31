package com.mtxrii.wikiforge.alpinebackend.column.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ColumnPatchRequest(
        @Size(max = 120) String name,
        @Positive Integer position,
        @Size(max = 64) String color
) {
}
