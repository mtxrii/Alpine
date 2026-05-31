package com.mtxrii.wikiforge.alpinebackend.ticket.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByBoard_IdOrderByColumn_PositionAscPositionAscCreatedAtAsc(UUID boardId);

    @Query("select coalesce(max(t.position), 0) from Ticket t where t.column.id = :columnId")
    int maxPositionByColumnId(UUID columnId);
}
