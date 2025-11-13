package com.example.colaba.exception.task;

import com.example.colaba.exception.common.NotFoundException;

public class TaskNotFoundException extends NotFoundException {
    public TaskNotFoundException(Long id) {
        super("Task not found: ID " + id);
    }
}
