package com.example.colaba.task.dto.comment;

import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
        @Size(max = 500, message = "Content must be between 1 and 500 characters")
        String content
) {
}
