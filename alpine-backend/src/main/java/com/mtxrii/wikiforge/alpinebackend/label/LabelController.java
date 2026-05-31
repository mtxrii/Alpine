package com.mtxrii.wikiforge.alpinebackend.label;

import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.label.dto.LabelPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.label.dto.LabelRequest;
import com.mtxrii.wikiforge.alpinebackend.label.dto.LabelResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LabelController {
    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @PostMapping("/boards/{boardId}/labels")
    ResponseEntity<LabelResponse> create(@PathVariable UUID boardId, @Valid @RequestBody LabelRequest request) {
        return ResponseEntity.ok(labelService.create(boardId, request));
    }

    @PatchMapping("/labels/{labelId}")
    LabelResponse update(@PathVariable UUID labelId, @Valid @RequestBody LabelPatchRequest request) {
        return labelService.update(labelId, request);
    }

    @DeleteMapping("/labels/{labelId}")
    ResponseEntity<Void> delete(@PathVariable UUID labelId) {
        labelService.delete(labelId);
        return ResponseEntity.noContent().build();
    }
}
