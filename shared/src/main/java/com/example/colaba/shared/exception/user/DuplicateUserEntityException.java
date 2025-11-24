package com.example.colaba.shared.exception.user;

import com.example.colaba.shared.exception.common.DuplicateEntityException;

public class DuplicateUserEntityException extends DuplicateEntityException {
    public DuplicateUserEntityException(String identifier) {
        super(String.format("Duplicate user entity: " + identifier));
    }
}
