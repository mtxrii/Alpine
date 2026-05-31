package com.mtxrii.wikiforge.alpinebackend.column.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.column.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {
    List<BoardColumn> findByBoard_IdOrderByPositionAscCreatedAtAsc(UUID boardId);
}
