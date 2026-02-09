package com.example.colaba.file.application.mapper;

import com.example.colaba.file.application.dto.FileResponse;
import com.example.colaba.file.domain.entity.File;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {

    public FileResponse toDto(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .taskId(file.getTaskId())
                .uuid(file.getUuid())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedBy(file.getUploadedBy())
                .uploadedAt(file.getCreatedAt())
                .build();
    }
}