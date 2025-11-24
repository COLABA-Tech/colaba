package com.example.colaba.shared.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 20, message = "Name must not exceed 20 characters")
        String name,

        @NotNull(message = "Project ID is required")
        @Positive(message = "Project ID must be positive")
        Long projectId
) {
}
