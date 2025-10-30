package com.example.colaba.exception.task;

import com.example.colaba.exception.common.NotFoundException;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(String message) {
        super("Task not found: " + message);
    }
}
