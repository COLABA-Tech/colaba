package com.example.colaba.shared.common.exception.common;

import java.io.Serial;
import java.io.Serializable;

public class NotFoundException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
