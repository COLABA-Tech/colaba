package com.example.colaba.shared.common.exception.user;

import java.io.Serial;
import java.io.Serializable;

public class UserPasswordSameAsOldException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserPasswordSameAsOldException(String username) {
        super(String.format("Password is the same as old: USER " + username));
    }

    public UserPasswordSameAsOldException(Long id) {
        super(String.format("Password is the same as old: ID " + id));
    }
}
