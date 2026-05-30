package com.mtxrii.wikiforge.alpinebackend.board.entity;

import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseEntity;
import com.mtxrii.wikiforge.alpinebackend.user.entity.User;
import com.mtxrii.wikiforge.alpinebackend.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "boards")
@Getter
@Setter
public class Board extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
