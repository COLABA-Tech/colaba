package com.example.colaba.shared.exception.common;

import java.io.Serial;
import java.io.Serializable;

public class DuplicateEntityException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateEntityException(String message) {
        super(message);
    }
}
