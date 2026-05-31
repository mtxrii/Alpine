package com.mtxrii.wikiforge.alpinebackend.board.dto;

import java.util.UUID;

public record BoardMemberResponse(UUID id, String name, String email, String role) {
}
