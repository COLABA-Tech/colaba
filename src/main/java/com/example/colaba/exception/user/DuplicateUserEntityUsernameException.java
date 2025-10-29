package com.example.colaba.exception.user;

import com.example.colaba.exception.common.DuplicateEntityException;

public class DuplicateUserEntityUsernameException extends DuplicateEntityException {
    public DuplicateUserEntityUsernameException(String username) {
        super(String.format("USERNAME " + username));
    }
}
