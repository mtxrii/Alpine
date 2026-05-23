package com.mtxrii.wikiforge.alpinebackend.ticket;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.ColumnSummary;
import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.LabelResponse;
import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.UserSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/boards/{boardId}/tickets")
    List<TicketResponse> list(@PathVariable UUID boardId) {
        return ticketService.listByBoard(boardId);
    }

    @PostMapping("/boards/{boardId}/tickets")
    ResponseEntity<TicketResponse> create(@PathVariable UUID boardId, @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.create(boardId, request));
    }

    @GetMapping("/tickets/{ticketId}")
    TicketResponse get(@PathVariable UUID ticketId) {
        return ticketService.get(ticketId);
    }

    @PatchMapping("/tickets/{ticketId}")
    TicketResponse update(@PathVariable UUID ticketId, @RequestBody TicketPatchRequest request) {
        return ticketService.update(ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/move")
    TicketResponse move(@PathVariable UUID ticketId, @RequestBody MoveTicketRequest request) {
        return ticketService.move(ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/assignees")
    TicketResponse assign(@PathVariable UUID ticketId, @RequestBody AssignUserRequest request) {
        return ticketService.assign(ticketId, request.userId());
    }

    @DeleteMapping("/tickets/{ticketId}/assignees/{userId}")
    TicketResponse unassign(@PathVariable UUID ticketId, @PathVariable UUID userId) {
        return ticketService.unassign(ticketId, userId);
    }

    @PostMapping("/tickets/{ticketId}/labels")
    TicketResponse addLabel(@PathVariable UUID ticketId, @RequestBody AddLabelRequest request) {
        return ticketService.addLabel(ticketId, request.labelId());
    }

    @DeleteMapping("/tickets/{ticketId}/labels/{labelId}")
    TicketResponse removeLabel(@PathVariable UUID ticketId, @PathVariable UUID labelId) {
        return ticketService.removeLabel(ticketId, labelId);
    }

    @DeleteMapping("/tickets/{ticketId}")
    ResponseEntity<Void> delete(@PathVariable UUID ticketId) {
        ticketService.delete(ticketId);
        return ResponseEntity.noContent().build();
    }

    public record TicketRequest(UUID columnId, String title, String description, String priority,
                                String status, Integer position, UUID createdBy, Instant dueDate) {
    }

    public record TicketPatchRequest(UUID columnId, String title, String description, String priority,
                                     String status, Integer position, Boolean archived, Instant dueDate) {
    }

    public record MoveTicketRequest(UUID sourceColumnId, UUID destinationColumnId, Integer newPosition) {
    }

    public record AssignUserRequest(UUID userId) {
    }

    public record AddLabelRequest(UUID labelId) {
    }

    public record TicketResponse(UUID id, UUID boardId, String title, String description, String priority,
                                 String status, ColumnSummary column, Integer position,
                                 List<UserSummary> assignees, List<LabelResponse> labels,
                                 UserSummary createdBy, Instant dueDate, Boolean archived,
                                 Instant createdAt, Instant updatedAt) {
    }
}
