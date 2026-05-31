package com.mtxrii.wikiforge.alpinebackend.board.dto;

import java.util.UUID;

public record BoardRequest(UUID workspaceId, String name, String description, UUID createdBy) {
}
