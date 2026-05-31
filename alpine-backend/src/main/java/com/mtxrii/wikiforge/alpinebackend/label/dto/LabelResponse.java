package com.mtxrii.wikiforge.alpinebackend.label.dto;

import java.util.UUID;

public record LabelResponse(UUID id, UUID boardId, String name, String color) {
}
