package com.mtxrii.wikiforge.alpinebackend.ticket.entity;

import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseAuditEntity;
import com.mtxrii.wikiforge.alpinebackend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ticket_assignees")
@Getter
@Setter
public class TicketAssignee extends BaseAuditEntity {

    @EmbeddedId
    private TicketAssigneeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketId")
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;
}
