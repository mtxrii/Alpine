package com.mtxrii.wikiforge.alpinebackend.workspace;

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

import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceMemberRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceMemberResponse;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspacePatchRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WorkspaceControllerTest {
    private WorkspaceService workspaceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        workspaceService = mock(WorkspaceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkspaceController(workspaceService)).build();
    }

    @Test
    void listReturnsWorkspaces() throws Exception {
        WorkspaceResponse workspace = workspace();
        when(workspaceService.list()).thenReturn(List.of(workspace));

        mockMvc.perform(get("/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(workspace.id().toString()))
                .andExpect(jsonPath("$[0].name").value(workspace.name()));
    }

    @Test
    void createReturnsWorkspace() throws Exception {
        WorkspaceResponse workspace = workspace();
        UUID ownerId = workspace.ownerId();
        when(workspaceService.create(new WorkspaceRequest(
                "Alpine",
                ownerId
        ))).thenReturn(workspace);

        mockMvc.perform(post("/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alpine",
                                  "ownerId": "%s"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspace.id().toString()))
                .andExpect(jsonPath("$.name").value(workspace.name()));
    }

    @Test
    void getReturnsWorkspace() throws Exception {
        WorkspaceResponse workspace = workspace();
        when(workspaceService.get(workspace.id())).thenReturn(workspace);

        mockMvc.perform(get("/workspaces/{workspaceId}", workspace.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspace.id().toString()))
                .andExpect(jsonPath("$.name").value(workspace.name()));
    }

    @Test
    void updateReturnsWorkspace() throws Exception {
        WorkspaceResponse workspace = workspace();
        when(workspaceService.update(eq(workspace.id()), eq(new WorkspacePatchRequest("Alpine v2"))))
                .thenReturn(workspace);

        mockMvc.perform(patch("/workspaces/{workspaceId}", workspace.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Alpine v2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspace.id().toString()));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        mockMvc.perform(delete("/workspaces/{workspaceId}", workspaceId))
                .andExpect(status().isNoContent());

        verify(workspaceService).delete(workspaceId);
    }

    @Test
    void listMembersReturnsMembers() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceMemberResponse member = new WorkspaceMemberResponse(userId, "Alice", "alice@example.com", "member");
        when(workspaceService.listMembers(workspaceId)).thenReturn(List.of(member));

        mockMvc.perform(get("/workspaces/{workspaceId}/members", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].role").value("member"));
    }

    @Test
    void addMemberReturnsMembers() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceMemberResponse member = new WorkspaceMemberResponse(userId, "Alice", "alice@example.com", "member");
        when(workspaceService.addMember(eq(workspaceId), eq(new WorkspaceMemberRequest(userId, "member"))))
                .thenReturn(List.of(member));

        mockMvc.perform(post("/workspaces/{workspaceId}/members", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": "%s",
                                  "role": "member"
                                }
                                """.formatted(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].role").value("member"));
    }

    @Test
    void removeMemberReturnsNoContent() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/workspaces/{workspaceId}/members/{userId}", workspaceId, userId))
                .andExpect(status().isNoContent());

        verify(workspaceService).removeMember(workspaceId, userId);
    }

    private WorkspaceResponse workspace() {
        return new WorkspaceResponse(
                UUID.randomUUID(),
                "Alpine",
                UUID.randomUUID(),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
    }
}
