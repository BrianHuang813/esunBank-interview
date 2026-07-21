package com.esunbank.library.business.model;

public record BookSummary(
        String isbn,
        String name,
        String author,
        String introduction,
        long inventoryCount,
        long availableCount
) {
}
