package com.mtxrii.wikiforge.alpinebackend.column.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ReorderColumnsRequest(
        @NotEmpty List<@NotNull UUID> orderedColumnIds
) {
}
