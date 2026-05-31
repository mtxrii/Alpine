package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddLabelRequest(@NotNull UUID labelId) {
}
