package com.example.colaba.shared.common.dto.project;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        OffsetDateTime createdAt
) {
}
