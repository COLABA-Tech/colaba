package com.example.colaba.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDto {

    private Long id;
    private Long taskId;
    private String originalFilename;
    private String contentType;
    private Long size;
    private Long uploadedBy;
    private OffsetDateTime uploadedAt;
}