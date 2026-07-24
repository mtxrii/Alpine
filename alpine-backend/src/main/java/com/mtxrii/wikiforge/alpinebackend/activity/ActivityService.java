package com.mtxrii.wikiforge.alpinebackend.activity;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.activity.dto.ActivityResponse;
import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class ActivityService extends JdbcSupport {
    public static final String TICKET_CREATED = "ticket_created";
    public static final String TICKET_MOVED = "ticket_moved";
    public static final String TICKET_ASSIGNED = "ticket_assigned";
    public static final String TICKET_UPDATED = "ticket_updated";
    public static final String COMMENT_ADDED = "comment_added";

    private final ObjectMapper objectMapper;

    private final RowMapper<ActivityResponse> mapper = (rs, rowNum) -> new ActivityResponse(
            rs.getObject("id", UUID.class),
            rs.getObject("board_id", UUID.class),
            rs.getObject("ticket_id", UUID.class),
            rs.getObject("user_id", UUID.class),
            rs.getString("user_name"),
            rs.getString("action"),
            rs.getString("metadata_text"),
            instant(rs, "created_at")
    );

    public ActivityService(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        super(jdbc);
        this.objectMapper = objectMapper;
    }

    public void log(UUID boardId, UUID ticketId, UUID userId, String action, Map<String, ?> metadata) {
        jdbc.update("""
                INSERT INTO activity_logs (board_id, ticket_id, user_id, action, metadata)
                VALUES (:boardId, :ticketId, :userId, :action, CAST(:metadata AS jsonb))
                """, new MapSqlParameterSource()
                .addValue("boardId", boardId)
                .addValue("ticketId", ticketId)
                .addValue("userId", userId)
                .addValue("action", action)
                .addValue("metadata", toJson(metadata)));
    }

    public java.util.List<ActivityResponse> listByBoard(UUID boardId) {
        return jdbc.query(baseSelect() + " WHERE a.board_id = :boardId ORDER BY a.created_at DESC LIMIT 100",
                Map.of("boardId", boardId), mapper);
    }

    public java.util.List<ActivityResponse> listByTicket(UUID ticketId) {
        return jdbc.query(baseSelect() + " WHERE a.ticket_id = :ticketId ORDER BY a.created_at DESC LIMIT 100",
                Map.of("ticketId", ticketId), mapper);
    }

    private String baseSelect() {
        return """
                SELECT a.*, a.metadata::text AS metadata_text, u.name AS user_name
                FROM activity_logs a
                JOIN users u ON u.id = a.user_id
                """;
    }

    private String toJson(Map<String, ?> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Unable to serialize activity metadata", exception);
        }
    }
}
