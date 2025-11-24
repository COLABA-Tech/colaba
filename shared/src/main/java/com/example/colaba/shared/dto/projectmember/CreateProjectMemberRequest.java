package com.example.colaba.shared.dto.projectmember;

import com.example.colaba.project.entity.projectmember.ProjectRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProjectMemberRequest(
        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,
        ProjectRole role
) {
}
