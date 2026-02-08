package com.example.colaba.task.domain.entity;

import com.example.colaba.task.domain.enums.TaskPriority;
import com.example.colaba.task.domain.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class Task {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long projectId;
    private Long assigneeId;
    private Long reporterId;
    private LocalDate dueDate;
}