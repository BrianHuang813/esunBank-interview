package com.esunbank.library.common.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ApiResponse<T>(T data, OffsetDateTime timestamp) {

    private static final ZoneId TAIPEI = ZoneId.of("Asia/Taipei");

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, OffsetDateTime.now(TAIPEI));
    }
}
