package com.mtxrii.wikiforge.alpinebackend.comment.entity;

import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseEntity;
import com.mtxrii.wikiforge.alpinebackend.ticket.entity.Ticket;
import com.mtxrii.wikiforge.alpinebackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String body;
}
