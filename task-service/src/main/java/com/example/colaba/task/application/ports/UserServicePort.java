package com.example.colaba.task.application.ports;

public interface UserServicePort {
    boolean userExists(Long userId);

    boolean isAdmin(Long userId);
}