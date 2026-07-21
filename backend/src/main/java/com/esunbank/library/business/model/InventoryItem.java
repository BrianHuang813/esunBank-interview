package com.esunbank.library.business.model;

import java.time.LocalDateTime;

public record InventoryItem(long inventoryId, String isbn, LocalDateTime storeTime, String status) {
}
