package com.example.colaba.file.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class File {
    private Long id;
    private Long taskId;
    private UUID uuid;
    private String originalFilename;
    private String contentType;
    private Long size;
    private Long uploadedBy;
    private OffsetDateTime createdAt;
}