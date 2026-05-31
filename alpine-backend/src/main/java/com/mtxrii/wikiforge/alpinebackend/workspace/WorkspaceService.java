package com.mtxrii.wikiforge.alpinebackend.workspace;

import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.JdbcSupport;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspacePatchRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceService extends JdbcSupport {
    private final RowMapper<WorkspaceResponse> mapper = (rs, rowNum) -> new WorkspaceResponse(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getObject("owner_id", UUID.class),
            instant(rs, "created_at"),
            instant(rs, "updated_at")
    );

    public WorkspaceService(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    public java.util.List<WorkspaceResponse> list() {
        return jdbc.query("SELECT * FROM workspaces ORDER BY created_at DESC", mapper);
    }

    @Transactional
    public WorkspaceResponse create(WorkspaceRequest request) {
        UUID id = insertReturningId("""
                INSERT INTO workspaces (name, owner_id)
                VALUES (:name, :ownerId)
                RETURNING id
                """, Map.of("name", request.name(), "ownerId", request.ownerId()));
        jdbc.update("""
                INSERT INTO workspace_members (workspace_id, user_id, role)
                VALUES (:workspaceId, :userId, 'owner')
                ON CONFLICT DO NOTHING
                """, new MapSqlParameterSource(Map.of("workspaceId", id, "userId", request.ownerId())));
        return get(id);
    }

    public WorkspaceResponse get(UUID id) {
        return required("Workspace", id, "SELECT * FROM workspaces WHERE id = :id", Map.of("id", id), mapper);
    }

    public WorkspaceResponse update(UUID id, WorkspacePatchRequest request) {
        ensureUpdated("Workspace", id, update("""
                UPDATE workspaces
                SET name = COALESCE(:name, name), updated_at = NOW()
                WHERE id = :id
                """, params("id", id, "name", request.name())));
        return get(id);
    }

    public void delete(UUID id) {
        ensureUpdated("Workspace", id, update("DELETE FROM workspaces WHERE id = :id", Map.of("id", id)));
    }
}
