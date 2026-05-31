package com.mtxrii.wikiforge.alpinebackend.column.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ColumnRequest(
        @NotBlank @Size(max = 120) String name,
        @Positive Integer position,
        @Size(max = 64) String color
) {
}
