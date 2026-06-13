package com.mtxrii.wikiforge.alpinebackend.ticket;

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

import com.mtxrii.wikiforge.alpinebackend.common.dto.ColumnSummary;
import com.mtxrii.wikiforge.alpinebackend.common.dto.UserSummary;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.MoveTicketRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketRequest;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TicketControllerTest {
    private TicketService ticketService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ticketService = mock(TicketService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TicketController(ticketService)).build();
    }

    @Test
    void listReturnsTicketsForBoard() throws Exception {
        TicketResponse ticket = ticket();
        when(ticketService.listByBoard(ticket.boardId())).thenReturn(List.of(ticket));

        mockMvc.perform(get("/boards/{boardId}/tickets", ticket.boardId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(ticket.id().toString()))
                .andExpect(jsonPath("$[0].title").value(ticket.title()));
    }

    @Test
    void createReturnsTicket() throws Exception {
        TicketResponse ticket = ticket();
        UUID columnId = ticket.column().id();
        UUID createdBy = ticket.createdBy().id();
        when(ticketService.create(eq(ticket.boardId()), eq(new TicketRequest(
                columnId,
                "Write API docs",
                "Document ticket endpoints",
                "high",
                "open",
                1000,
                createdBy,
                Instant.parse("2026-02-01T00:00:00Z")
        )))).thenReturn(ticket);

        mockMvc.perform(post("/boards/{boardId}/tickets", ticket.boardId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "columnId": "%s",
                                  "title": "Write API docs",
                                  "description": "Document ticket endpoints",
                                  "priority": "high",
                                  "status": "open",
                                  "position": 1000,
                                  "createdBy": "%s",
                                  "dueDate": "2026-02-01T00:00:00Z"
                                }
                                """.formatted(columnId, createdBy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.id().toString()))
                .andExpect(jsonPath("$.column.id").value(columnId.toString()))
                .andExpect(jsonPath("$.position").value(ticket.position()));
    }

    @Test
    void getReturnsTicket() throws Exception {
        TicketResponse ticket = ticket();
        when(ticketService.get(ticket.id())).thenReturn(ticket);

        mockMvc.perform(get("/tickets/{ticketId}", ticket.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.id().toString()))
                .andExpect(jsonPath("$.boardId").value(ticket.boardId().toString()));
    }

    @Test
    void updateReturnsTicket() throws Exception {
        TicketResponse ticket = ticket();
        when(ticketService.update(eq(ticket.id()), eq(new TicketPatchRequest(
                null,
                "Ship ticket APIs",
                null,
                "urgent",
                null,
                2000,
                false,
                null
        )))).thenReturn(ticket);

        mockMvc.perform(patch("/tickets/{ticketId}", ticket.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Ship ticket APIs",
                                  "priority": "urgent",
                                  "position": 2000,
                                  "archived": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.id().toString()));
    }

    @Test
    void moveReturnsTicketAtDestinationPosition() throws Exception {
        TicketResponse ticket = ticket();
        UUID sourceColumnId = ticket.column().id();
        UUID destinationColumnId = UUID.randomUUID();
        when(ticketService.move(eq(ticket.id()), eq(new MoveTicketRequest(sourceColumnId, destinationColumnId, 2500))))
                .thenReturn(ticket);

        mockMvc.perform(post("/tickets/{ticketId}/move", ticket.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceColumnId": "%s",
                                  "destinationColumnId": "%s",
                                  "newPosition": 2500
                                }
                                """.formatted(sourceColumnId, destinationColumnId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticket.id().toString()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        UUID ticketId = UUID.randomUUID();

        mockMvc.perform(delete("/tickets/{ticketId}", ticketId))
                .andExpect(status().isNoContent());

        verify(ticketService).delete(ticketId);
    }

    private TicketResponse ticket() {
        UUID columnId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        return new TicketResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Write API docs",
                "Document ticket endpoints",
                "high",
                "open",
                new ColumnSummary(columnId, "Todo"),
                1000,
                List.of(),
                List.of(),
                new UserSummary(createdBy, "Ada Lovelace"),
                Instant.parse("2026-02-01T00:00:00Z"),
                false,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
    }
}
