package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.time.Instant;
import java.util.UUID;

public record TicketPatchRequest(UUID columnId, String title, String description, String priority,
                                 String status, Integer position, Boolean archived, Instant dueDate) {
}
