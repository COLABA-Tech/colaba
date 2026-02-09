package com.example.colaba.shared.common.application.dto.common;

public record PaginationRequest(
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    public static PaginationRequest defaultRequest() {
        return new PaginationRequest(0, 20, "id", "ASC");
    }

    public PaginationRequest withPage(int page) {
        return new PaginationRequest(page, size, sortBy, sortDirection);
    }

    public PaginationRequest withSize(int size) {
        return new PaginationRequest(page, size, sortBy, sortDirection);
    }

    public PaginationRequest withPageAndSize(int page, int size) {
        return new PaginationRequest(page, size, sortBy, sortDirection);
    }
}
