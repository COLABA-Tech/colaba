package com.example.colaba.task.application.dto.comment;

public record CommentResponse(
        Long id,
        Long taskId,
        Long userId,
        String content
) {
}
