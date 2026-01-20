package com.example.colaba.task.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        Long projectId,
        Long assigneeId,
        Long reporterId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dueDate
) {
}
