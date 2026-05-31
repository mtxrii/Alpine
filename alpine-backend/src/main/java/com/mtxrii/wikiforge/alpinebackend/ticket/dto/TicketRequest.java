package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotNull UUID columnId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 10000) String description,
        @Pattern(regexp = "low|medium|high|urgent") String priority,
        @Size(max = 80) String status,
        @Positive Integer position,
        @NotNull UUID createdBy,
        Instant dueDate
) {
}
