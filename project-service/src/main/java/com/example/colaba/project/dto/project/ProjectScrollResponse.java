package com.example.colaba.project.dto.project;

import com.example.colaba.shared.common.dto.project.ProjectResponse;

import java.util.List;

public record ProjectScrollResponse(
        List<ProjectResponse> projects,
        boolean hasMore,
        Long total
) {
}
