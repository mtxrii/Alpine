package com.mtxrii.wikiforge.alpinebackend.board;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardRequest;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class BoardControllerTest {
    private BoardService boardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        boardService = mock(BoardService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BoardController(boardService)).build();
    }

    @Test
    void listReturnsBoards() throws Exception {
        BoardResponse board = board();
        when(boardService.list()).thenReturn(List.of(board));

        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(board.id().toString()))
                .andExpect(jsonPath("$[0].name").value(board.name()));
    }

    @Test
    void createReturnsBoard() throws Exception {
        BoardResponse board = board();
        when(boardService.create(new BoardRequest(
                board.workspaceId(),
                "Roadmap",
                "Quarterly planning",
                board.createdBy()
        ))).thenReturn(board);

        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workspaceId": "%s",
                                  "name": "Roadmap",
                                  "description": "Quarterly planning",
                                  "createdBy": "%s"
                                }
                                """.formatted(board.workspaceId(), board.createdBy())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(board.id().toString()))
                .andExpect(jsonPath("$.name").value(board.name()));
    }

    @Test
    void getReturnsBoard() throws Exception {
        BoardResponse board = board();
        when(boardService.get(board.id())).thenReturn(board);

        mockMvc.perform(get("/boards/{boardId}", board.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(board.id().toString()))
                .andExpect(jsonPath("$.workspaceId").value(board.workspaceId().toString()));
    }

    @Test
    void updateReturnsBoard() throws Exception {
        BoardResponse board = board();
        when(boardService.update(eq(board.id()), eq(new BoardPatchRequest("Roadmap v2", null))))
                .thenReturn(board);

        mockMvc.perform(patch("/boards/{boardId}", board.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Roadmap v2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(board.id().toString()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        UUID boardId = UUID.randomUUID();

        mockMvc.perform(delete("/boards/{boardId}", boardId))
                .andExpect(status().isNoContent());

        verify(boardService).delete(boardId);
    }

    private BoardResponse board() {
        return new BoardResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Roadmap",
                "Quarterly planning",
                UUID.randomUUID(),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
    }
}
