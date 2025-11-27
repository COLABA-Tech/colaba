package com.example.colaba.shared.exception.comment;

import com.example.colaba.shared.exception.common.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}