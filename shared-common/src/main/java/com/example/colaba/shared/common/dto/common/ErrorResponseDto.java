package com.example.colaba.shared.common.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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