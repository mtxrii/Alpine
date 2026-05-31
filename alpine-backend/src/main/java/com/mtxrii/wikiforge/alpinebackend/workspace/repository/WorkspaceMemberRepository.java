package com.mtxrii.wikiforge.alpinebackend.workspace.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.workspace.entity.WorkspaceMember;
import com.mtxrii.wikiforge.alpinebackend.workspace.entity.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId> {
    List<WorkspaceMember> findByWorkspace_IdOrderByUser_NameAsc(UUID workspaceId);

    List<WorkspaceMember> findByUser_Id(UUID userId);
}
