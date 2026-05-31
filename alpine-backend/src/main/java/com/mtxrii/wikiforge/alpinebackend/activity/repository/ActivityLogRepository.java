package com.mtxrii.wikiforge.alpinebackend.activity.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.activity.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findTop100ByBoard_IdOrderByCreatedAtDesc(UUID boardId);

    List<ActivityLog> findTop100ByTicket_IdOrderByCreatedAtDesc(UUID ticketId);
}
