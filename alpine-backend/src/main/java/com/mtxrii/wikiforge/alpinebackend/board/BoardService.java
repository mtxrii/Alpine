package com.mtxrii.wikiforge.alpinebackend.board;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardFullResponse;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardMemberResponse;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardRequest;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardResponse;
import com.mtxrii.wikiforge.alpinebackend.column.ColumnService;
import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import com.mtxrii.wikiforge.alpinebackend.label.LabelService;
import com.mtxrii.wikiforge.alpinebackend.ticket.TicketService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BoardService extends JdbcSupport {
    private final ColumnService columnService;
    private final TicketService ticketService;
    private final LabelService labelService;
    private final RowMapper<BoardResponse> mapper = (rs, rowNum) -> new BoardResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("workspace_id", UUID.class),
            rs.getString("name"),
            rs.getString("description"),
            rs.getObject("created_by", UUID.class),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
    );

    public BoardService(NamedParameterJdbcTemplate jdbc, ColumnService columnService,
                        TicketService ticketService, LabelService labelService) {
        super(jdbc);
        this.columnService = columnService;
        this.ticketService = ticketService;
        this.labelService = labelService;
    }

    public java.util.List<BoardResponse> list() {
        return jdbc.query("SELECT * FROM boards ORDER BY created_at DESC", mapper);
    }

    public BoardResponse create(BoardRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO boards (workspace_id, name, description, created_by)
                VALUES (:workspaceId, :name, :description, :createdBy)
                RETURNING id
                """, params("workspaceId", request.workspaceId(), "name", request.name(),
                "description", request.description(), "createdBy", request.createdBy()));
        return get(id);
    }

    public BoardResponse get(UUID id) {
        return required("Board", id, "SELECT * FROM boards WHERE id = :id", Map.of("id", id), mapper);
    }

    public BoardFullResponse getFull(UUID id) {
        BoardResponse board = get(id);
        var members = jdbc.query("""
                SELECT u.id, u.name, u.email, wm.role
                FROM workspace_members wm
                JOIN users u ON u.id = wm.user_id
                WHERE wm.workspace_id = :workspaceId
                ORDER BY u.name
                """, Map.of("workspaceId", board.workspaceId()), (rs, rowNum) -> new BoardMemberResponse(
                rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("email"), rs.getString("role")));
        return new BoardFullResponse(board, columnService.list(id), ticketService.listByBoard(id),
                members, labelService.listByBoard(id));
    }

    public BoardResponse update(UUID id, BoardPatchRequest request) {
        ensureUpdated("Board", id, update("""
                UPDATE boards
                SET name = COALESCE(:name, name),
                    description = COALESCE(:description, description),
                    updated_at = NOW()
                WHERE id = :id
                """, params("id", id, "name", request.name(), "description", request.description())));
        return get(id);
    }

    public void delete(UUID id) {
        ensureUpdated("Board", id, update("DELETE FROM boards WHERE id = :id", Map.of("id", id)));
    }
}
