package com.mtxrii.wikiforge.alpinebackend.ticket.entity;

import com.mtxrii.wikiforge.alpinebackend.board.entity.Board;
import com.mtxrii.wikiforge.alpinebackend.column.entity.BoardColumn;
import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseEntity;
import com.mtxrii.wikiforge.alpinebackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id", nullable = false)
    private BoardColumn column;

    @Column(nullable = false)
    private String title;

    private String description;

    private String priority;

    private String status;

    @Column(nullable = false)
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "due_date")
    private Instant dueDate;

    private Boolean archived = false;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<com.mtxrii.wikiforge.alpinebackend.ticket.entity.TicketAssignee> assignees;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<com.mtxrii.wikiforge.alpinebackend.ticket.entity.TicketLabel> labels;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<com.mtxrii.wikiforge.alpinebackend.comment.entity.Comment> comments;
}
