package com.mtxrii.wikiforge.alpinebackend.board.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByWorkspace_IdOrderByCreatedAtDesc(UUID workspaceId);
}
