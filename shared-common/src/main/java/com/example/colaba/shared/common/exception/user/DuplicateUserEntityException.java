package com.example.colaba.shared.common.exception.user;

import com.example.colaba.shared.common.exception.common.DuplicateEntityException;

public class DuplicateUserEntityException extends DuplicateEntityException {
    public DuplicateUserEntityException(String identifier) {
        super(String.format("Duplicate user entity: " + identifier));
    }
}
