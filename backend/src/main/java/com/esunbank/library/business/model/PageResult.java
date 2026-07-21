package com.esunbank.library.business.model;

import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public long totalPages() {
        return size == 0 ? 0 : (totalElements + size - 1) / size;
    }
}
