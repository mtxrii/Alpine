package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.time.Instant;
import java.util.UUID;

public record TicketRequest(UUID columnId, String title, String description, String priority,
                            String status, Integer position, UUID createdBy, Instant dueDate) {
}
