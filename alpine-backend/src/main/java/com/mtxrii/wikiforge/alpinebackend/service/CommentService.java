package com.mtxrii.wikiforge.alpinebackend.service;

import static com.mtxrii.wikiforge.alpinebackend.api.controller.CommentController.CommentPatchRequest;
import static com.mtxrii.wikiforge.alpinebackend.api.controller.CommentController.CommentRequest;
import static com.mtxrii.wikiforge.alpinebackend.api.controller.CommentController.CommentResponse;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommentService extends JdbcSupport {
    private final RowMapper<CommentResponse> mapper = (rs, rowNum) -> new CommentResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("ticket_id", UUID.class),
            rs.getObject("author_id", UUID.class),
            rs.getString("author_name"),
            rs.getString("body"),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
    );

    public CommentService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    public java.util.List<CommentResponse> list(UUID ticketId) {
        return jdbc.query(baseSelect() + " WHERE c.ticket_id = :ticketId ORDER BY c.created_at",
                Map.of("ticketId", ticketId), mapper);
    }

    public CommentResponse create(UUID ticketId, CommentRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO comments (ticket_id, author_id, body)
                VALUES (:ticketId, :authorId, :body)
                RETURNING id
                """, Map.of("ticketId", ticketId, "authorId", request.authorId(), "body", request.body()));
        return get(id);
    }

    public CommentResponse get(UUID id) {
        return required("Comment", id, baseSelect() + " WHERE c.id = :id", Map.of("id", id), mapper);
    }

    public CommentResponse update(UUID id, CommentPatchRequest request) {
        ensureUpdated("Comment", id, update("""
                UPDATE comments
                SET body = COALESCE(:body, body), updated_at = NOW()
                WHERE id = :id
                """, params("id", id, "body", request.body())));
        return get(id);
    }

    public void delete(UUID id) {
        ensureUpdated("Comment", id, update("DELETE FROM comments WHERE id = :id", Map.of("id", id)));
    }

    private String baseSelect() {
        return """
                SELECT c.*, u.name AS author_name
                FROM comments c
                JOIN users u ON u.id = c.author_id
                """;
    }
}
