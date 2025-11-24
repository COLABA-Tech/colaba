package com.example.colaba.shared.exception.task;

import com.example.colaba.shared.exception.common.NotFoundException;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task not found: ID " + id);
    }
}
