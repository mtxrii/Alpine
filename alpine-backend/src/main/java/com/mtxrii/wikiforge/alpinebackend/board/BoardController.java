package com.mtxrii.wikiforge.alpinebackend.board;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardFullResponse;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardPatchRequest;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardRequest;
import com.mtxrii.wikiforge.alpinebackend.board.dto.BoardResponse;
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
    ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardRequest request) {
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
    BoardResponse update(@PathVariable UUID boardId, @Valid @RequestBody BoardPatchRequest request) {
        return boardService.update(boardId, request);
    }

    @DeleteMapping("/{boardId}")
    ResponseEntity<Void> delete(@PathVariable UUID boardId) {
        boardService.delete(boardId);
        return ResponseEntity.noContent().build();
    }
}
