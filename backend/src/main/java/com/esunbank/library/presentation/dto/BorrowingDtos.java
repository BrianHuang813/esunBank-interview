package com.esunbank.library.presentation.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Positive;

public final class BorrowingDtos {

    private BorrowingDtos() {
    }

    public record BorrowRequest(
            @Positive(message = "inventoryId 必須是正整數")
            long inventoryId
    ) {
    }

    public record Response(
            long id,
            long inventoryId,
            String isbn,
            String bookName,
            String author,
            String inventoryStatus,
            OffsetDateTime borrowingTime,
            OffsetDateTime returnTime
    ) {
    }
}
