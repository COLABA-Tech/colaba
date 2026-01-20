package com.example.colaba.shared.common.dto.project;

import lombok.Builder;

@Builder
public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long ownerId
) {
}
