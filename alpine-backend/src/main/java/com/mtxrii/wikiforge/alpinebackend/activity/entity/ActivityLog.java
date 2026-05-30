package com.mtxrii.wikiforge.alpinebackend.activity.entity;

import com.mtxrii.wikiforge.alpinebackend.board.entity.Board;
import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseEntity;
import com.mtxrii.wikiforge.alpinebackend.ticket.entity.Ticket;
import com.mtxrii.wikiforge.alpinebackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
public class ActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;
}
