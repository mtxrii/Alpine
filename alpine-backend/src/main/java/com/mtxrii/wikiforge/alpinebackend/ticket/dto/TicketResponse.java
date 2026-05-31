package com.mtxrii.wikiforge.alpinebackend.ticket.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.dto.ColumnSummary;
import com.mtxrii.wikiforge.alpinebackend.common.dto.UserSummary;
import com.mtxrii.wikiforge.alpinebackend.label.dto.LabelResponse;

public record TicketResponse(UUID id, UUID boardId, String title, String description, String priority,
                             String status, ColumnSummary column, Integer position,
                             List<UserSummary> assignees, List<LabelResponse> labels,
                             UserSummary createdBy, Instant dueDate, Boolean archived,
                             Instant createdAt, Instant updatedAt) {
}
