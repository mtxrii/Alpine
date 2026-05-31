package com.mtxrii.wikiforge.alpinebackend.workspace.dto;

import java.util.UUID;

public record WorkspaceRequest(String name, UUID ownerId) {
}
