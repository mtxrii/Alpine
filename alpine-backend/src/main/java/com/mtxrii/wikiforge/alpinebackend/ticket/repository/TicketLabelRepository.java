package com.mtxrii.wikiforge.alpinebackend.ticket.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.ticket.entity.TicketLabel;
import com.mtxrii.wikiforge.alpinebackend.ticket.entity.TicketLabelId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketLabelRepository extends JpaRepository<TicketLabel, TicketLabelId> {
    List<TicketLabel> findByTicket_IdOrderByLabel_NameAsc(UUID ticketId);
}
