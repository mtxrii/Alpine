package com.mtxrii.wikiforge.alpinebackend.column;

import static com.mtxrii.wikiforge.alpinebackend.column.ColumnController.ColumnPatchRequest;
import static com.mtxrii.wikiforge.alpinebackend.column.ColumnController.ColumnRequest;
import static com.mtxrii.wikiforge.alpinebackend.column.ColumnController.ReorderColumnsRequest;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.ColumnResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ColumnService extends JdbcSupport {
    private final RowMapper<ColumnResponse> mapper = (rs, rowNum) -> new ColumnResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("board_id", UUID.class),
            rs.getString("name"),
            rs.getInt("position"),
            rs.getString("color"),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
    );

    public ColumnService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    public java.util.List<ColumnResponse> list(UUID boardId) {
        return jdbc.query("SELECT * FROM columns WHERE board_id = :boardId ORDER BY position, created_at",
                Map.of("boardId", boardId), mapper);
    }

    public ColumnResponse create(UUID boardId, ColumnRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO columns (board_id, name, position, color)
                VALUES (:boardId, :name, :position, :color)
                RETURNING id
                """, params("boardId", boardId, "name", request.name(),
                "position", request.position() == null ? nextPosition(boardId) : request.position(),
                "color", request.color()));
        return get(id);
    }

    @Transactional
    public java.util.List<ColumnResponse> reorder(UUID boardId, ReorderColumnsRequest request) {
        int position = 1000;
        for (UUID columnId : request.orderedColumnIds()) {
            ensureUpdated("Column", columnId, update("""
                    UPDATE columns
                    SET position = :position, updated_at = NOW()
                    WHERE id = :id AND board_id = :boardId
                    """, Map.of("id", columnId, "boardId", boardId, "position", position)));
            position += 1000;
        }
        return list(boardId);
    }

    public ColumnResponse get(UUID id) {
        return required("Column", id, "SELECT * FROM columns WHERE id = :id", Map.of("id", id), mapper);
    }

    public ColumnResponse update(UUID id, ColumnPatchRequest request) {
        ensureUpdated("Column", id, update("""
                UPDATE columns
                SET name = COALESCE(:name, name),
                    position = COALESCE(:position, position),
                    color = COALESCE(:color, color),
                    updated_at = NOW()
                WHERE id = :id
                """, params("id", id, "name", request.name(), "position", request.position(), "color", request.color())));
        return get(id);
    }

    public void delete(UUID id) {
        ensureUpdated("Column", id, update("DELETE FROM columns WHERE id = :id", Map.of("id", id)));
    }

    private int nextPosition(UUID boardId) {
        Integer max = jdbc.queryForObject("SELECT COALESCE(MAX(position), 0) FROM columns WHERE board_id = :boardId",
                Map.of("boardId", boardId), Integer.class);
        return (max == null ? 0 : max) + 1000;
    }
}
