package com.example.colaba.shared.dto.project;

import java.time.OffsetDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        OffsetDateTime createdAt
) {
}
