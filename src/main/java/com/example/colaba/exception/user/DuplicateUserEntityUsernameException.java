package com.example.colaba.exception.user;

public class DuplicateUserEntityUsernameException extends DuplicateUserEntityException {
    public DuplicateUserEntityUsernameException(String username) {
        super(String.format("USERNAME " + username));
    }
}
