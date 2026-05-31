package com.mtxrii.wikiforge.alpinebackend.workspace;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspacePatchRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceRequest;
import com.mtxrii.wikiforge.alpinebackend.workspace.dto.WorkspaceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    List<WorkspaceResponse> list() {
        return workspaceService.list();
    }

    @PostMapping
    ResponseEntity<WorkspaceResponse> create(@Valid @RequestBody WorkspaceRequest request) {
        return ResponseEntity.ok(workspaceService.create(request));
    }

    @GetMapping("/{workspaceId}")
    WorkspaceResponse get(@PathVariable UUID workspaceId) {
        return workspaceService.get(workspaceId);
    }

    @PatchMapping("/{workspaceId}")
    WorkspaceResponse update(@PathVariable UUID workspaceId, @Valid @RequestBody WorkspacePatchRequest request) {
        return workspaceService.update(workspaceId, request);
    }

    @DeleteMapping("/{workspaceId}")
    ResponseEntity<Void> delete(@PathVariable UUID workspaceId) {
        workspaceService.delete(workspaceId);
        return ResponseEntity.noContent().build();
    }
}
