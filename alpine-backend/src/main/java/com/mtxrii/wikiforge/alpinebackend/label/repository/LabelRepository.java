package com.mtxrii.wikiforge.alpinebackend.label.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.label.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findByBoard_IdOrderByNameAsc(UUID boardId);
}
