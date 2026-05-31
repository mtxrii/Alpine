package com.mtxrii.wikiforge.alpinebackend.column.dto;

import java.util.List;
import java.util.UUID;

public record ReorderColumnsRequest(List<UUID> orderedColumnIds) {
}
