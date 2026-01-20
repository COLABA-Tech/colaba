package com.example.colaba.project.dto.project;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(
        @NotBlank(message = "Project name is required") String name,
        String description
) {
}
