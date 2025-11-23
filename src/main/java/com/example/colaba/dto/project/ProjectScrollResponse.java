package com.example.colaba.dto.project;

import java.util.List;

public record ProjectScrollResponse(
        List<ProjectResponse> projects,
        boolean hasMore,
        Long total
) {
}
