package com.mtxrii.wikiforge.alpinebackend.board.dto;

import java.util.List;

import com.mtxrii.wikiforge.alpinebackend.column.dto.ColumnResponse;
import com.mtxrii.wikiforge.alpinebackend.label.dto.LabelResponse;
import com.mtxrii.wikiforge.alpinebackend.ticket.dto.TicketResponse;

public record BoardFullResponse(BoardResponse board, List<ColumnResponse> columns,
                                List<TicketResponse> tickets, List<BoardMemberResponse> members,
                                List<LabelResponse> labels) {
}
