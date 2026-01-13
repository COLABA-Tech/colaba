package com.example.colaba.shared.common.exception.user;

import com.example.colaba.shared.common.exception.common.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super(String.format("User not found: ID " + id));
    }

    public UserNotFoundException(String username) {
        super(String.format("User not found: USERNAME " + username));
    }
}
