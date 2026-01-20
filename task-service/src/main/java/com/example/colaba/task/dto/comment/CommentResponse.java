package com.example.colaba.task.dto.comment;

public record CommentResponse(
        Long id,
        Long taskId,
        Long userId,
        String content
) {
}
