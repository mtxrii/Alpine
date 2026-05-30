package com.mtxrii.wikiforge.alpinebackend.ticket.entity;

import com.mtxrii.wikiforge.alpinebackend.common.entity.BaseAuditEntity;
import com.mtxrii.wikiforge.alpinebackend.label.entity.Label;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ticket_labels")
@Getter
@Setter
public class TicketLabel extends BaseAuditEntity {

    @EmbeddedId
    private TicketLabelId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketId")
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("labelId")
    @JoinColumn(name = "label_id")
    private Label label;
}
