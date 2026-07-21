package com.esunbank.library.common.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;

public record ApiError(
        String code,
        String message,
        OffsetDateTime timestamp,
        String path,
        Map<String, String> errors
) {
    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");

    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, OffsetDateTime.now(TAIPEI), path, null);
    }

    public static ApiError validation(String message, String path, Map<String, String> errors) {
        return new ApiError("VALIDATION_FAILED", message, OffsetDateTime.now(TAIPEI), path, errors);
    }
}
