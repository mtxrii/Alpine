package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MoveTicketRequest(
        @NotNull UUID sourceColumnId,
        @NotNull UUID destinationColumnId,
        @NotNull @Positive Integer newPosition
) {
}
