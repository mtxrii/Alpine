package com.mtxrii.wikiforge.alpinebackend.workspace.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    List<Workspace> findByOwner_IdOrderByCreatedAtDesc(UUID ownerId);
}
