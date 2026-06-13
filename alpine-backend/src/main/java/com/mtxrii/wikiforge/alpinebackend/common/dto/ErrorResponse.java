package com.mtxrii.wikiforge.alpinebackend.common.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fields
) {
    /** Convenience factory for errors without field-level detail. */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, null);
    }

    /** Convenience factory for validation errors that include per-field detail. */
    public static ErrorResponse ofFields(String message, Map<String, String> fields) {
        return new ErrorResponse(Instant.now(), 400, "Bad Request", message, fields);
    }
}
