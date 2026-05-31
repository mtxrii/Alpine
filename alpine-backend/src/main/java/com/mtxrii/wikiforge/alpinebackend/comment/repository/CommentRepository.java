package com.mtxrii.wikiforge.alpinebackend.comment.repository;

import java.util.List;
import java.util.UUID;

import com.mtxrii.wikiforge.alpinebackend.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTicket_IdOrderByCreatedAtAsc(UUID ticketId);
}
