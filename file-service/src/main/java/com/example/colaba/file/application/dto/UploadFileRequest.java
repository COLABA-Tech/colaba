package com.example.colaba.file.application.dto;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record UploadFileRequest(
        Long taskId,
        Long uploadedBy,
        List<MultipartFile> files
) {
}