package com.mtxrii.wikiforge.alpinebackend.label.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LabelRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 64) String color
) {
}
