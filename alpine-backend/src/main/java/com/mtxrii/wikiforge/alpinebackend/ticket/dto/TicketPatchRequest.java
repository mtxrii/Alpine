package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TicketPatchRequest(
        UUID columnId,
        @Size(max = 200) String title,
        @Size(max = 10000) String description,
        @Pattern(regexp = "low|medium|high|urgent") String priority,
        @Size(max = 80) String status,
        @Positive Integer position,
        Boolean archived,
        Instant dueDate
) {
}
