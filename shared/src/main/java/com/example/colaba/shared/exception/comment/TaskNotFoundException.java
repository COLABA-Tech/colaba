package com.example.colaba.shared.exception.comment;

import com.example.colaba.shared.exception.common.NotFoundException;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task not found: " + id);
    }
}