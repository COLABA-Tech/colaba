package com.example.colaba.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        Long projectId,
        String projectName,
        Long assigneeId,
        String assigneeUsername,
        Long reporterId,
        String reporterUsername,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dueDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}
