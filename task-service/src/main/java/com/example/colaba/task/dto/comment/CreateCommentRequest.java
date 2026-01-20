package com.example.colaba.task.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotNull(message = "Task ID is required")
        @Positive(message = "Task ID must be a positive number")
        Long taskId,

        @NotBlank(message = "Content cannot be blank")
        @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
        String content
) {
}