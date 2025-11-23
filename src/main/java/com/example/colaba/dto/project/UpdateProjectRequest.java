package com.example.colaba.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @NotBlank(message = "Project name must not be blank")
        @Size(max = 255, message = "Project name must be shorter than 255 characters")
        String name,

        @Size(max = 1000, message = "Description must be shorter than 1000 characters")
        String description,

        @NotNull(message = "ownerId must not be null")
        Long ownerId

) {
}
