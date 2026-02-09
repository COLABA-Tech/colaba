package com.example.colaba.shared.common.application.dto.common;

import java.util.List;

public record PagedResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size
) {
    public static <T> PagedResult<T> of(List<T> content, int currentPage, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PagedResult<>(content, totalElements, totalPages, currentPage, size);
    }
}
