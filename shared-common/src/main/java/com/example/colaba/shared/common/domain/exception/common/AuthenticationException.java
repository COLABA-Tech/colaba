package com.example.colaba.shared.common.domain.exception.common;

import java.io.Serial;
import java.io.Serializable;

public class AuthenticationException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
