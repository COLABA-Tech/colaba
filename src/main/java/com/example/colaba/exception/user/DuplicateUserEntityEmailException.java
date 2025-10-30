package com.example.colaba.exception.user;

public class DuplicateUserEntityEmailException extends DuplicateUserEntityException {
    public DuplicateUserEntityEmailException(String username) {
        super(String.format("EMAIL " + username));
    }
}
