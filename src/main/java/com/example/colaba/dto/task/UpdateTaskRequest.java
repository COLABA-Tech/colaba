package com.example.colaba.dto.task;

import com.example.colaba.entity.task.TaskPriority;
import com.example.colaba.entity.task.TaskStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UpdateTaskRequest {
    @Size(min = 1, max = 200, message = "Title must not exceed 200 characters")
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    @Positive(message = "Assignee ID must be positive")
    private Long assigneeId;
    private LocalDate dueDate;
}
