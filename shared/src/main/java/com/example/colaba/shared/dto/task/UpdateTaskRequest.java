package com.example.colaba.shared.dto.task;

import com.example.colaba.shared.entity.task.TaskPriority;
import com.example.colaba.shared.entity.task.TaskStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(min = 1, max = 200, message = "Title must not exceed 200 characters")
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        @Positive(message = "Assignee ID must be positive")
        Long assigneeId,
        LocalDate dueDate
) {
}
