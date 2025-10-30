package com.example.colaba.dto.comment;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
public record UpdateCommentRequest(
        @Positive(message = "Comment ID must be a positive number")
        Long id,  // Опционально: если id передаётся в теле; иначе в пути (/comments/{id})

        @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
        String content  // Nullable: если null — не обновляем
) {
}
