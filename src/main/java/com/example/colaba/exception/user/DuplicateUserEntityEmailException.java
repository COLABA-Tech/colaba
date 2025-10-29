package com.example.colaba.exception.user;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateUserEntityEmailException extends DuplicateEntityException {
    public DuplicateUserEntityEmailException(String username) {
        super(String.format("EMAIL " + username));
    }
}
