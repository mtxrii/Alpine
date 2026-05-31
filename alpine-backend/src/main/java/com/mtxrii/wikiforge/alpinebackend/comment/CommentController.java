package com.mtxrii.wikiforge.alpinebackend.comment;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.comment.dto.CommentPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.comment.dto.CommentRequest;
import com.mtxrii.wikiforge.alpinebackend.comment.dto.CommentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/tickets/{ticketId}/comments")
    List<CommentResponse> list(@PathVariable UUID ticketId) {
        return commentService.list(ticketId);
    }

    @PostMapping("/tickets/{ticketId}/comments")
    ResponseEntity<CommentResponse> create(@PathVariable UUID ticketId, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.create(ticketId, request));
    }

    @PatchMapping("/comments/{commentId}")
    CommentResponse update(@PathVariable UUID commentId, @Valid @RequestBody CommentPatchRequest request) {
        return commentService.update(commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    ResponseEntity<Void> delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}
