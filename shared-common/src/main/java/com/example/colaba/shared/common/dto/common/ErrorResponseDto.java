package com.example.colaba.shared.common.dto.common;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ErrorResponseDto(
        String error,
        int status,
        String message,
        String path,
        OffsetDateTime timestamp,
        Object details
) {
    public ErrorResponseDto(String error, int status, String message) {
        this(error, status, message, null, OffsetDateTime.now(), null);
    }
}