package com.example.colaba.exception.comment;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(Long id) {
        super("Task not found: " + id);
    }
}