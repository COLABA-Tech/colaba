package com.example.colaba.shared.dto.comment;

import java.time.OffsetDateTime;

public record CommentResponse(
        Long id,
        Long taskId,
        Long userId,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
