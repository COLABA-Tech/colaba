package com.example.colaba.task.dto.task;

import com.example.dto.FileDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

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
        List<FileDto> attachments
) {
        // Статический метод для тестов (без attachments)
        public static TaskResponse withoutAttachments(
                Long id,
                String title,
                String description,
                String status,
                String priority,
                Long projectId,
                Long assigneeId,
                Long reporterId,
                LocalDate dueDate
        ) {
                return new TaskResponse(id, title, description, status, priority, projectId, assigneeId, reporterId, dueDate, Collections.emptyList());
        }
}
