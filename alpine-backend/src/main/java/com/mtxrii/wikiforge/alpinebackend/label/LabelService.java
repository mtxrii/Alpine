package com.mtxrii.wikiforge.alpinebackend.label;

import static com.mtxrii.wikiforge.alpinebackend.label.LabelController.LabelPatchRequest;
import static com.mtxrii.wikiforge.alpinebackend.label.LabelController.LabelRequest;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.LabelResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LabelService extends JdbcSupport {
    private final RowMapper<LabelResponse> mapper = (rs, rowNum) -> new LabelResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("board_id", UUID.class),
            rs.getString("name"),
            rs.getString("color")
    );

    public LabelService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    public java.util.List<LabelResponse> listByBoard(UUID boardId) {
        return jdbc.query("SELECT * FROM labels WHERE board_id = :boardId ORDER BY name",
                Map.of("boardId", boardId), mapper);
    }

    public LabelResponse create(UUID boardId, LabelRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO labels (board_id, name, color)
                VALUES (:boardId, :name, :color)
                RETURNING id
                """, Map.of("boardId", boardId, "name", request.name(), "color", request.color()));
        return get(id);
    }

    public LabelResponse get(UUID id) {
        return required("Label", id, "SELECT * FROM labels WHERE id = :id", Map.of("id", id), mapper);
    }

    public LabelResponse update(UUID id, LabelPatchRequest request) {
        ensureUpdated("Label", id, update("""
                UPDATE labels
                SET name = COALESCE(:name, name),
                    color = COALESCE(:color, color)
                WHERE id = :id
                """, params("id", id, "name", request.name(), "color", request.color())));
        return get(id);
    }

    public void delete(UUID id) {
        ensureUpdated("Label", id, update("DELETE FROM labels WHERE id = :id", Map.of("id", id)));
    }
}
