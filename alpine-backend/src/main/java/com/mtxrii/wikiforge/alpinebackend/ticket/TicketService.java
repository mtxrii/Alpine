package com.mtxrii.wikiforge.alpinebackend.ticket;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import com.mtxrii.wikiforge.alpinebackend.common.dto.ColumnSummary;
import com.mtxrii.wikiforge.alpinebackend.common.dto.UserSummary;
import com.mtxrii.wikiforge.alpinebackend.label.dto.LabelResponse;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.MoveTicketRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService extends JdbcSupport {
    private final RowMapper<TicketRow> rowMapper = (rs, rowNum) -> new TicketRow(
            rs.getObject("id", UUID.class),
            rs.getObject("board_id", UUID.class),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("priority"),
            rs.getString("status"),
            rs.getObject("column_id", UUID.class),
            rs.getString("column_name"),
            rs.getInt("position"),
            rs.getObject("created_by", UUID.class),
            rs.getString("created_by_name"),
            instant(rs, "due_date"),
            rs.getBoolean("archived"),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
    );

    public TicketService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    public java.util.List<TicketResponse> listByBoard(UUID boardId) {
        return jdbc.query(baseSelect() + " WHERE t.board_id = :boardId ORDER BY c.position, t.position, t.created_at",
                        Map.of("boardId", boardId), rowMapper)
                .stream().map(this::hydrate).toList();
    }

    public TicketResponse create(UUID boardId, TicketRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO tickets (board_id, column_id, title, description, priority, status, position, created_by, due_date)
                VALUES (:boardId, :columnId, :title, :description, :priority, :status, :position, :createdBy, :dueDate)
                RETURNING id
                """, params("boardId", boardId, "columnId", request.columnId(), "title", request.title(),
                "description", request.description(), "priority", request.priority(), "status", request.status(),
                "position", request.position() == null ? nextPosition(request.columnId()) : request.position(),
                "createdBy", request.createdBy(), "dueDate", request.dueDate()));
        return get(id);
    }

    public TicketResponse get(UUID id) {
        TicketRow row = required("Ticket", id, baseSelect() + " WHERE t.id = :id", Map.of("id", id), rowMapper);
        return hydrate(row);
    }

    public TicketResponse update(UUID id, TicketPatchRequest request) {
        ensureUpdated("Ticket", id, update("""
                UPDATE tickets
                SET column_id = COALESCE(:columnId, column_id),
                    title = COALESCE(:title, title),
                    description = COALESCE(:description, description),
                    priority = COALESCE(:priority, priority),
                    status = COALESCE(:status, status),
                    position = COALESCE(:position, position),
                    archived = COALESCE(:archived, archived),
                    due_date = COALESCE(:dueDate, due_date),
                    updated_at = NOW()
                WHERE id = :id
                """, params("id", id, "columnId", request.columnId(), "title", request.title(),
                "description", request.description(), "priority", request.priority(), "status", request.status(),
                "position", request.position(), "archived", request.archived(), "dueDate", request.dueDate())));
        return get(id);
    }

    @Transactional
    public TicketResponse move(UUID id, MoveTicketRequest request) {
        ensureUpdated("Ticket", id, update("""
                UPDATE tickets
                SET column_id = :destinationColumnId, position = :newPosition
                WHERE id = :id AND column_id = :sourceColumnId
                """, Map.of("id", id, "sourceColumnId", request.sourceColumnId(),
                "destinationColumnId", request.destinationColumnId(), "newPosition", request.newPosition())));
        return get(id);
    }

    public TicketResponse assign(UUID ticketId, UUID userId) {
        jdbc.update("""
                INSERT INTO ticket_assignees (ticket_id, user_id)
                VALUES (:ticketId, :userId)
                ON CONFLICT DO NOTHING
                """, Map.of("ticketId", ticketId, "userId", userId));
        return get(ticketId);
    }

    public TicketResponse unassign(UUID ticketId, UUID userId) {
        jdbc.update("DELETE FROM ticket_assignees WHERE ticket_id = :ticketId AND user_id = :userId",
                Map.of("ticketId", ticketId, "userId", userId));
        return get(ticketId);
    }

    public TicketResponse addLabel(UUID ticketId, UUID labelId) {
        jdbc.update("""
                INSERT INTO ticket_labels (ticket_id, label_id)
                VALUES (:ticketId, :labelId)
                ON CONFLICT DO NOTHING
                """, Map.of("ticketId", ticketId, "labelId", labelId));
        return get(ticketId);
    }

    public TicketResponse removeLabel(UUID ticketId, UUID labelId) {
        jdbc.update("DELETE FROM ticket_labels WHERE ticket_id = :ticketId AND label_id = :labelId",
                Map.of("ticketId", ticketId, "labelId", labelId));
        return get(ticketId);
    }

    public void delete(UUID id) {
        ensureUpdated("Ticket", id, update("DELETE FROM tickets WHERE id = :id", Map.of("id", id)));
    }

    private TicketResponse hydrate(TicketRow row) {
        var assignees = jdbc.query("""
                SELECT u.id, u.name
                FROM ticket_assignees ta
                JOIN users u ON u.id = ta.user_id
                WHERE ta.ticket_id = :ticketId
                ORDER BY u.name
                """, Map.of("ticketId", row.id()), (rs, rowNum) ->
                new UserSummary(rs.getObject("id", UUID.class), rs.getString("name")));
        var labels = jdbc.query("""
                SELECT l.id, l.board_id, l.name, l.color
                FROM ticket_labels tl
                JOIN labels l ON l.id = tl.label_id
                WHERE tl.ticket_id = :ticketId
                ORDER BY l.name
                """, Map.of("ticketId", row.id()), (rs, rowNum) -> new LabelResponse(
                rs.getObject("id", UUID.class), rs.getObject("board_id", UUID.class),
                rs.getString("name"), rs.getString("color")));
        return new TicketResponse(row.id(), row.boardId(), row.title(), row.description(), row.priority(), row.status(),
                new ColumnSummary(row.columnId(), row.columnName()), row.position(), assignees, labels,
                new UserSummary(row.createdBy(), row.createdByName()), row.dueDate(), row.archived(),
                row.createdAt(), row.updatedAt());
    }

    private String baseSelect() {
        return """
                SELECT t.*, c.name AS column_name, u.name AS created_by_name
                FROM tickets t
                JOIN columns c ON c.id = t.column_id
                JOIN users u ON u.id = t.created_by
                """;
    }

    private int nextPosition(UUID columnId) {
        Integer max = jdbc.queryForObject("SELECT COALESCE(MAX(position), 0) FROM tickets WHERE column_id = :columnId",
                Map.of("columnId", columnId), Integer.class);
        return (max == null ? 0 : max) + 1000;
    }

    private record TicketRow(UUID id, UUID boardId, String title, String description, String priority,
                             String status, UUID columnId, String columnName, Integer position,
                             UUID createdBy, String createdByName, java.time.Instant dueDate,
                             Boolean archived, java.time.Instant createdAt, java.time.Instant updatedAt) {
    }
}
