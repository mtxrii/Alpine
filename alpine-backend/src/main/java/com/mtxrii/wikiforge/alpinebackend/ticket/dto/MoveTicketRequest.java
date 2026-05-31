package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.util.UUID;

public record MoveTicketRequest(UUID sourceColumnId, UUID destinationColumnId, Integer newPosition) {
}
