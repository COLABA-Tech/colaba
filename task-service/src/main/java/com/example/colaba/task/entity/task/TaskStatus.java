package com.example.colaba.task.entity.task;

import lombok.Getter;

@Getter
public enum TaskStatus {
    TODO("To Do", "Task needs to be done"),
    IN_PROGRESS("In Progress", "Task is being worked on"),
    IN_REVIEW("In Review", "Task is under review"),
    BLOCKED("Blocked", "Task is blocked by another task"),
    DONE("Done", "Task is completed"),
    CANCELLED("Cancelled", "Task was cancelled");

    private final String name;
    private final String description;

    TaskStatus(String displayName, String description) {
        this.name = displayName;
        this.description = description;
    }

    public static TaskStatus getDefault() {
        return TODO;
    }
}