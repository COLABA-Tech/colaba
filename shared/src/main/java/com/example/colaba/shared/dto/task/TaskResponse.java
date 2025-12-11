package com.example.colaba.shared.dto.task;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;

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
        LocalDate dueDate,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        OffsetDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        OffsetDateTime updatedAt
) {
}
