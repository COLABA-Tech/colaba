package com.example.colaba.dto.project;

import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.time.LocalDateTime;

public record ProjectResponse (
    Long id,
    String name,
    String description,
    Long ownerId,
    String ownerName,
    LocalDateTime createdAt
) {}
