package com.example.colaba.shared.common.domain.exception.user;

public class DuplicateUserEntityEmailException extends DuplicateUserEntityException {
    public DuplicateUserEntityEmailException(String username) {
        super(String.format("EMAIL " + username));
    }
}
