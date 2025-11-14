package com.example.colaba.dto.comment;

import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
        // Убрала id — он в @PathVariable
        @Size(max = 500, message = "Content must be between 1 and 500 characters")  // Убрала min=1: allow null/empty для partial update
        String content  // Nullable: если null — не обновляем в сервисе
) {
}
