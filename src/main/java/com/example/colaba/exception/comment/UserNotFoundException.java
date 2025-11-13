package com.example.colaba.exception.comment;

import com.example.colaba.exception.common.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}