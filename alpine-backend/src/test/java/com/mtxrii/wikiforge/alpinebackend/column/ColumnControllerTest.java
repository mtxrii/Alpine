package com.mtxrii.wikiforge.alpinebackend.column;

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

import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnRequest;
import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ColumnControllerTest {
    private ColumnService columnService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        columnService = mock(ColumnService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ColumnController(columnService)).build();
    }

    @Test
    void listReturnsColumnsForBoard() throws Exception {
        ColumnResponse column = column();
        when(columnService.list(column.boardId())).thenReturn(List.of(column));

        mockMvc.perform(get("/boards/{boardId}/columns", column.boardId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(column.id().toString()))
                .andExpect(jsonPath("$[0].position").value(column.position()));
    }

    @Test
    void createReturnsColumn() throws Exception {
        ColumnResponse column = column();
        when(columnService.create(eq(column.boardId()), eq(new ColumnRequest("Todo", 1000, "#2f80ed"))))
                .thenReturn(column);

        mockMvc.perform(post("/boards/{boardId}/columns", column.boardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Todo",
                                  "position": 1000,
                                  "color": "#2f80ed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(column.id().toString()))
                .andExpect(jsonPath("$.boardId").value(column.boardId().toString()))
                .andExpect(jsonPath("$.position").value(column.position()));
    }

    @Test
    void updateReturnsColumn() throws Exception {
        ColumnResponse column = column();
        when(columnService.update(eq(column.id()), eq(new ColumnPatchRequest("Doing", 2000, null))))
                .thenReturn(column);

        mockMvc.perform(patch("/columns/{columnId}", column.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Doing",
                                  "position": 2000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(column.id().toString()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        UUID columnId = UUID.randomUUID();

        mockMvc.perform(delete("/columns/{columnId}", columnId))
                .andExpect(status().isNoContent());

        verify(columnService).delete(columnId);
    }

    private ColumnResponse column() {
        return new ColumnResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Todo",
                1000,
                "#2f80ed",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
    }
}
