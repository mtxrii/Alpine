package com.mtxrii.wikiforge.alpinebackend.ticket.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketLabelId implements Serializable {
    private UUID ticketId;
    private UUID labelId;
}
