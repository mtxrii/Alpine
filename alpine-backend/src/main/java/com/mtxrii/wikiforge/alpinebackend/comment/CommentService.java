package com.mtxrii.wikiforge.alpinebackend.comment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.activity.ActivityService;
import com.mtxrii.wikiforge.alpinebackend.comment.dto.CommentPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.comment.dto.CommentRequest;
import com.mtxrii.wikiforge.alpinebackend.comment.dto.CommentResponse;
import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService extends JdbcSupport {
    private final ActivityService activityService;

    private final RowMapper<CommentResponse> mapper = (rs, rowNum) -> new CommentResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("ticket_id", UUID.class),
            rs.getObject("author_id", UUID.class),
            rs.getString("author_name"),
            rs.getString("body"),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
    );

    public CommentService(NamedParameterJdbcTemplate jdbc, ActivityService activityService) {
        super(jdbc);
        this.activityService = activityService;
    }

    public java.util.List<CommentResponse> list(UUID ticketId) {
        return jdbc.query(baseSelect() + " WHERE c.ticket_id = :ticketId ORDER BY c.created_at",
                Map.of("ticketId", ticketId), mapper);
    }

    @Transactional
    public CommentResponse create(UUID ticketId, CommentRequest request) {
        TicketActivityTarget target = requireTicketActivityTarget(ticketId);
        UUID id = insertReturningId("""
                INSERT INTO comments (ticket_id, author_id, body)
                VALUES (:ticketId, :authorId, :body)
                RETURNING id
                """, Map.of("ticketId", ticketId, "authorId", request.authorId(), "body", request.body()));
        CommentResponse comment = get(id);
        activityService.log(target.boardId(), ticketId, request.authorId(), ActivityService.COMMENT_ADDED, metadata(
                "commentId", id,
                "ticketTitle", target.ticketTitle()
        ));
        return comment;
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

    private TicketActivityTarget requireTicketActivityTarget(UUID ticketId) {
        return required("Ticket", ticketId, """
                SELECT board_id, title
                FROM tickets
                WHERE id = :id
                """, Map.of("id", ticketId), (rs, rowNum) -> new TicketActivityTarget(
                rs.getObject("board_id", UUID.class),
                rs.getString("title")
        ));
    }

    private Map<String, Object> metadata(Object... keysAndValues) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            metadata.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return metadata;
    }

    private record TicketActivityTarget(UUID boardId, String ticketTitle) {
    }
}
