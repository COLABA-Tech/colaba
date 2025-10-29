package com.example.colaba.exception.user;

import com.example.colaba.exception.common.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super("User not found: " + message);
    }
}