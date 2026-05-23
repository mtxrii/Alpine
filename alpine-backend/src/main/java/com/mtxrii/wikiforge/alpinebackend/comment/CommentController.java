package com.mtxrii.wikiforge.alpinebackend.comment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    ResponseEntity<CommentResponse> create(@PathVariable UUID ticketId, @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.create(ticketId, request));
    }

    @PatchMapping("/comments/{commentId}")
    CommentResponse update(@PathVariable UUID commentId, @RequestBody CommentPatchRequest request) {
        return commentService.update(commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    ResponseEntity<Void> delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }

    public record CommentRequest(UUID authorId, String body) {
    }

    public record CommentPatchRequest(String body) {
    }

    public record CommentResponse(UUID id, UUID ticketId, UUID authorId, String authorName,
                                  String body, Instant createdAt, Instant updatedAt) {
    }
}
