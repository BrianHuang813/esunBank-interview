package com.esunbank.library.business.model;

import java.util.List;

public record BookDetail(
        String isbn,
        String name,
        String author,
        String introduction,
        long inventoryCount,
        long availableCount,
        List<InventoryItem> inventory
) {
}
