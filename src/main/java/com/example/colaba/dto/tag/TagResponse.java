package com.example.colaba.dto.tag;

public record TagResponse(
        Long id,
        String name,
        Long projectId,
        String projectName
) {
}
