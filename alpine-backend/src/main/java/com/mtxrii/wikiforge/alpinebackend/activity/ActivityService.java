package com.mtxrii.wikiforge.alpinebackend.activity;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.activity.dto.ActivityResponse;
import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ActivityService extends JdbcSupport {
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

    public ActivityService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
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
}
