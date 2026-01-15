package com.example.colaba.shared.common.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
}
