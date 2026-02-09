package com.example.colaba.file.application.dto;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record FileResponse(
        Long id,
        Long taskId,
        UUID uuid,
        String originalFilename,
        String contentType,
        Long size,
        Long uploadedBy,
        OffsetDateTime uploadedAt
) {
}