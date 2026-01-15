package com.example.colaba.shared.common.exception.task;

import com.example.colaba.shared.common.exception.common.NotFoundException;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task not found: ID " + id);
    }
}
