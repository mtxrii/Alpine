package com.mtxrii.wikiforge.alpinebackend.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.ticket.dto.MoveTicketRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class TicketServiceTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("alpine")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TicketService ticketService;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("DELETE FROM workspaces", Map.of());
        jdbc.update("DELETE FROM users", Map.of());
    }

    @Test
    void createUsesSparsePositionsWhenPositionIsOmitted() {
        BoardFixture fixture = createBoardFixture();

        TicketResponse first = ticketService.create(fixture.boardId(), new TicketRequest(
                fixture.todoColumnId(),
                "First ticket",
                null,
                "medium",
                "open",
                null,
                fixture.userId(),
                null
        ));
        TicketResponse second = ticketService.create(fixture.boardId(), new TicketRequest(
                fixture.todoColumnId(),
                "Second ticket",
                null,
                "medium",
                "open",
                null,
                fixture.userId(),
                null
        ));

        assertThat(first.position()).isEqualTo(1000);
        assertThat(second.position()).isEqualTo(2000);
    }

    @Test
    void moveUpdatesOnlyColumnAndPosition() {
        BoardFixture fixture = createBoardFixture();
        Instant dueDate = Instant.parse("2026-03-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-01T00:00:00Z");
        UUID ticketId = insertTicket(fixture, dueDate, updatedAt);

        TicketResponse moved = ticketService.move(ticketId, new MoveTicketRequest(
                fixture.todoColumnId(),
                fixture.doingColumnId(),
                2500
        ));

        assertThat(moved.column().id()).isEqualTo(fixture.doingColumnId());
        assertThat(moved.position()).isEqualTo(2500);
        assertThat(moved.title()).isEqualTo("Move me");
        assertThat(moved.description()).isEqualTo("Original description");
        assertThat(moved.priority()).isEqualTo("high");
        assertThat(moved.status()).isEqualTo("open");
        assertThat(moved.createdBy().id()).isEqualTo(fixture.userId());
        assertThat(moved.dueDate()).isEqualTo(dueDate);
        assertThat(moved.archived()).isFalse();
        assertThat(moved.updatedAt()).isEqualTo(updatedAt);
    }

    private BoardFixture createBoardFixture() {
        UUID userId = queryUuid("""
                INSERT INTO users (email, name, password_hash)
                VALUES (:email, :name, :passwordHash)
                RETURNING id
                """, Map.of(
                "email", UUID.randomUUID() + "@example.com",
                "name", "Ada Lovelace",
                "passwordHash", "$2a$10$abcdefghijklmnopqrstuuoHjsSxeHlv2CJrPd0fp2jnYYtLNsTgW"));
        UUID workspaceId = queryUuid("""
                INSERT INTO workspaces (name, owner_id)
                VALUES (:name, :ownerId)
                RETURNING id
                """, Map.of("name", "Workspace", "ownerId", userId));
        jdbc.update("""
                INSERT INTO workspace_members (workspace_id, user_id, role)
                VALUES (:workspaceId, :userId, 'owner')
                """, Map.of("workspaceId", workspaceId, "userId", userId));
        UUID boardId = queryUuid("""
                INSERT INTO boards (workspace_id, name, created_by)
                VALUES (:workspaceId, :name, :createdBy)
                RETURNING id
                """, Map.of("workspaceId", workspaceId, "name", "Board", "createdBy", userId));
        UUID todoColumnId = insertColumn(boardId, "Todo", 1000);
        UUID doingColumnId = insertColumn(boardId, "Doing", 2000);
        return new BoardFixture(userId, boardId, todoColumnId, doingColumnId);
    }

    private UUID insertColumn(UUID boardId, String name, int position) {
        return queryUuid("""
                INSERT INTO columns (board_id, name, position)
                VALUES (:boardId, :name, :position)
                RETURNING id
                """, Map.of("boardId", boardId, "name", name, "position", position));
    }

    private UUID insertTicket(BoardFixture fixture, Instant dueDate, Instant updatedAt) {
        return jdbc.queryForObject("""
                INSERT INTO tickets (
                    board_id, column_id, title, description, priority, status, position, created_by, due_date, updated_at
                )
                VALUES (
                    :boardId, :columnId, :title, :description, :priority, :status, :position, :createdBy, :dueDate, :updatedAt
                )
                RETURNING id
                """, new MapSqlParameterSource()
                .addValue("boardId", fixture.boardId())
                .addValue("columnId", fixture.todoColumnId())
                .addValue("title", "Move me")
                .addValue("description", "Original description")
                .addValue("priority", "high")
                .addValue("status", "open")
                .addValue("position", 1000)
                .addValue("createdBy", fixture.userId())
                .addValue("dueDate", Timestamp.from(dueDate))
                .addValue("updatedAt", Timestamp.from(updatedAt)), UUID.class);
    }

    private UUID queryUuid(String sql, Map<String, ?> params) {
        return jdbc.queryForObject(sql, params, UUID.class);
    }

    private record BoardFixture(UUID userId, UUID boardId, UUID todoColumnId, UUID doingColumnId) {
    }
}
