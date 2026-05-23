package com.mtxrii.wikiforge.alpinebackend.board;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.BoardResponse;
import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.ColumnResponse;
import com.mtxrii.wikiforge.alpinebackend.common.SimpleDtos.LabelResponse;
import com.mtxrii.wikiforge.alpinebackend.ticket.TicketController.TicketResponse;
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
@RequestMapping("/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    List<BoardResponse> list() {
        return boardService.list();
    }

    @PostMapping
    ResponseEntity<BoardResponse> create(@RequestBody BoardRequest request) {
        return ResponseEntity.ok(boardService.create(request));
    }

    @GetMapping("/{boardId}")
    BoardResponse get(@PathVariable UUID boardId) {
        return boardService.get(boardId);
    }

    @GetMapping("/{boardId}/full")
    BoardFullResponse getFull(@PathVariable UUID boardId) {
        return boardService.getFull(boardId);
    }

    @PatchMapping("/{boardId}")
    BoardResponse update(@PathVariable UUID boardId, @RequestBody BoardPatchRequest request) {
        return boardService.update(boardId, request);
    }

    @DeleteMapping("/{boardId}")
    ResponseEntity<Void> delete(@PathVariable UUID boardId) {
        boardService.delete(boardId);
        return ResponseEntity.noContent().build();
    }

    public record BoardRequest(UUID workspaceId, String name, String description, UUID createdBy) {
    }

    public record BoardPatchRequest(String name, String description) {
    }

    public record BoardFullResponse(BoardResponse board, List<ColumnResponse> columns,
                                    List<TicketResponse> tickets, List<BoardMemberResponse> members,
                                    List<LabelResponse> labels) {
    }

    public record BoardMemberResponse(UUID id, String name, String email, String role) {
    }
}
