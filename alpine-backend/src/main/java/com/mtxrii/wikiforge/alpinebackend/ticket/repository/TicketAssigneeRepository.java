package com.mtxrii.wikiforge.alpinebackend.ticket.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.ticket.entity.TicketAssignee;
import com.mtxrii.wikiforge.alpinebackend.ticket.entity.TicketAssigneeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAssigneeRepository extends JpaRepository<TicketAssignee, TicketAssigneeId> {
    List<TicketAssignee> findByTicket_IdOrderByUser_NameAsc(UUID ticketId);
}
