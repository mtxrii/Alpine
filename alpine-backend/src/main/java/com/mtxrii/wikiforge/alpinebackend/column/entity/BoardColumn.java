package com.mtxrii.wikiforge.alpinebackend.column.entity;

import com.mtxrii.wikiforge.alpinebackend.board.entity.Board;
import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseEntity;
import com.mtxrii.wikiforge.alpinebackend.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "columns")
@Getter
@Setter
public class BoardColumn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @jakarta.persistence.Column(nullable = false)
    private String name;

    @jakarta.persistence.Column(nullable = false)
    private Integer position;

    private String color;

    @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Ticket> tickets;
}
