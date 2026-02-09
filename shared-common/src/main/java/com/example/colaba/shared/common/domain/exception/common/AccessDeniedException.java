package com.example.colaba.shared.common.domain.exception.common;

import java.io.Serial;
import java.io.Serializable;

public class AccessDeniedException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}
