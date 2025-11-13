package com.example.colaba.exception.user;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateUserEntityException extends DuplicateEntityException {
    public DuplicateUserEntityException(String identifier) {
        super(String.format("Duplicate user entity: " + identifier));
    }
}
