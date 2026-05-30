package com.mtxrii.wikiforge.alpinebackend.workspace.entity;

import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseAuditEntity;
import com.mtxrii.wikiforge.alpinebackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workspace_members")
@Getter
@Setter
public class WorkspaceMember extends BaseAuditEntity {

    @EmbeddedId
    private WorkspaceMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String role;
}
