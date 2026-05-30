package com.mtxrii.wikiforge.alpinebackend.workspace.entity;

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
public class WorkspaceMemberId implements Serializable {
    private UUID workspaceId;
    private UUID userId;
}
