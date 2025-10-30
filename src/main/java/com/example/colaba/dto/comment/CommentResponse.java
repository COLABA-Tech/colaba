package com.example.colaba.dto.comment;
import java.time.ZonedDateTime;
public record CommentResponse(
        Long id,
        Long taskId,
        Long userId,
        String content,
        ZonedDateTime createdAt
) {
}
