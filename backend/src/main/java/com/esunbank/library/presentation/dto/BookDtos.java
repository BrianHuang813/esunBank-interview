package com.esunbank.library.presentation.dto;

import java.time.OffsetDateTime;
import java.util.List;

public final class BookDtos {

    private BookDtos() {
    }

    public record SummaryResponse(
            String isbn,
            String name,
            String author,
            String introduction,
            long inventoryCount,
            long availableCount
    ) {
    }

    public record InventoryResponse(
            long inventoryId,
            String status,
            OffsetDateTime storeTime
    ) {
    }

    public record DetailResponse(
            String isbn,
            String name,
            String author,
            String introduction,
            long inventoryCount,
            long availableCount,
            List<InventoryResponse> inventory
    ) {
    }
}
