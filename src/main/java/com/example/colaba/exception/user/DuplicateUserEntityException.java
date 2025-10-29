package com.example.colaba.exception.user;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateUserEntityException extends DuplicateEntityException {
    public DuplicateUserEntityException(String message) {
        super("Duplicate user entity: " + message);
    }
}
