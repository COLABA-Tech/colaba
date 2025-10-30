package com.example.colaba.dto.task;

import com.example.colaba.entity.task.TaskPriority;
import com.example.colaba.entity.task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        @NotNull(message = "Project ID is required")
        @Positive(message = "Project ID must be positive")
        Long projectId,
        @Positive(message = "Assignee ID must be positive")
        Long assigneeId,
        @NotNull(message = "Reporter ID is required")
        @Positive(message = "Reporter ID must be positive")
        Long reporterId,
        LocalDate dueDate
) {
    public CreateTaskRequest {
        if (status == null) {
            status = TaskStatus.getDefault();
        }
    }
}