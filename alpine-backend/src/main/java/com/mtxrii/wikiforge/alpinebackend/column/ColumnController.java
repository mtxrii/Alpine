package com.mtxrii.wikiforge.alpinebackend.column;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnRequest;
import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnResponse;
import com.mtxrii.wikiforge.alpinebackend.column.dto.ReorderColumnsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ColumnController {
    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @GetMapping("/boards/{boardId}/columns")
    List<ColumnResponse> list(@PathVariable UUID boardId) {
        return columnService.list(boardId);
    }

    @PostMapping("/boards/{boardId}/columns")
    ResponseEntity<ColumnResponse> create(@PathVariable UUID boardId, @RequestBody ColumnRequest request) {
        return ResponseEntity.ok(columnService.create(boardId, request));
    }

    @PostMapping("/boards/{boardId}/columns/reorder")
    List<ColumnResponse> reorder(@PathVariable UUID boardId, @RequestBody ReorderColumnsRequest request) {
        return columnService.reorder(boardId, request);
    }

    @PatchMapping("/columns/{columnId}")
    ColumnResponse update(@PathVariable UUID columnId, @RequestBody ColumnPatchRequest request) {
        return columnService.update(columnId, request);
    }

    @DeleteMapping("/columns/{columnId}")
    ResponseEntity<Void> delete(@PathVariable UUID columnId) {
        columnService.delete(columnId);
        return ResponseEntity.noContent().build();
    }
}
