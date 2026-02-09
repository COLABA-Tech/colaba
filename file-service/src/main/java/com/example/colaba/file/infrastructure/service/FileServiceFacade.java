package com.example.colaba.file.infrastructure.service;

import com.example.colaba.file.application.dto.FileResponse;
import com.example.colaba.file.application.dto.UploadFileRequest;
import com.example.colaba.file.application.service.FileService;
import com.example.colaba.file.domain.entity.File;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceFacade {

    private final FileService fileService;

    public List<FileResponse> uploadFiles(UploadFileRequest request) {
        return request.files().stream()
                .filter(file -> !file.isEmpty())
                .map(file -> saveOne(request, file))
                .collect(Collectors.toList());
    }

    private FileResponse saveOne(UploadFileRequest request, MultipartFile file) {
        try {
            return fileService.saveFile(
                    request.taskId(),
                    request.uploadedBy(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    public List<FileResponse> getAttachments(Long taskId) {
        return fileService.getAttachments(taskId);
    }

    public FileResponse getFileMetadata(Long fileId) {
        File file = fileService.getFileMetadataEntity(fileId);
        return FileResponse.builder()
                .id(file.getId())
                .taskId(file.getTaskId())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedBy(file.getUploadedBy())
                .uploadedAt(file.getCreatedAt())
                .build();
    }

    public Resource getFileContent(Long fileId) {
        byte[] content = fileService.getFileContent(fileId);
        if (content == null || content.length == 0) {
            throw new RuntimeException("File content is empty or not found");
        }
        return new ByteArrayResource(content);
    }
}