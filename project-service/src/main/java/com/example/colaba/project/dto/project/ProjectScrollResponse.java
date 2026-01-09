package com.example.colaba.project.dto.project;

import com.example.colaba.shared.dto.project.ProjectResponse;

import java.util.List;

public record ProjectScrollResponse(
        List<ProjectResponse> projects,
        boolean hasMore,
        Long total
) {
}
