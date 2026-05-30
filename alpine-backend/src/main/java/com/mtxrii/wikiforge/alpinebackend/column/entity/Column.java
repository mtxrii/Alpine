package com.mtxrii.wikiforge.alpinebackend.column.entity;

import com.mtxrii.wikiforge.alpinebackend.board.entity.Board;
import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "columns")
@Getter
@Setter
public class Column extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer position;

    private String color;
}
