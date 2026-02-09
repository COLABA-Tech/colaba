package com.example.colaba.shared.common.infrastructure.controller;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import org.springframework.data.domain.*;

public abstract class BaseController {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    static protected Pageable validatePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE, Sort.unsorted());
        }
        if (pageable.getPageSize() > MAX_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_SIZE, pageable.getSort());
        }
        return pageable;
    }

    static protected PaginationRequest convertToPaginationRequest(Pageable pageable) {
        Sort.Order order = pageable.getSort().isEmpty()
                ? Sort.Order.by("id")
                : pageable.getSort().iterator().next();

        return new PaginationRequest(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                order.getProperty(),
                order.getDirection().name()
        );
    }

    static protected <T> Page<T> convertToPage(PagedResult<T> result, Pageable pageable) {
        return new PageImpl<>(
                result.content(),
                pageable,
                result.totalElements()
        );
    }
}
