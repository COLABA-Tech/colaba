package com.example.colaba.shared.common.application.dto.common;

import java.util.List;

public record PagedResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int size
) {}
