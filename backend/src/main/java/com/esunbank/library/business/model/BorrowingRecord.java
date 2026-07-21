package com.esunbank.library.business.model;

import java.time.LocalDateTime;

public record BorrowingRecord(
        long id,
        long userId,
        long inventoryId,
        LocalDateTime borrowingTime,
        LocalDateTime returnTime,
        String inventoryStatus,
        String isbn,
        String bookName,
        String author
) {
}
