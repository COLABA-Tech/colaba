package com.example.colaba.shared.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required") String name,
        String description,
        @NotNull(message = "Owner ID is required") Long ownerId
) {
}
