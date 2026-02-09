package com.example.colaba.file.application.service;

import com.example.colaba.file.application.dto.FileResponse;
import com.example.colaba.file.application.mapper.FileMapper;
import com.example.colaba.file.application.ports.FileRepositoryPort;
import com.example.colaba.file.application.ports.FileStoragePort;
import com.example.colaba.file.domain.entity.File;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FileService {

    private final FileRepositoryPort fileRepository;
    private final FileStoragePort fileStorage;
    private final FileMapper fileMapper;

    public FileResponse saveFile(
            Long taskId,
            Long uploadedBy,
            String originalFilename,
            String contentType,
            long size,
            byte[] content
    ) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        UUID uuid = UUID.randomUUID();

        fileStorage.save(uuid, content);

        File file = File.builder()
                .taskId(taskId)
                .uuid(uuid)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .size(size)
                .uploadedBy(uploadedBy)
                .build();

        return fileMapper.toDto(fileRepository.save(file));
    }

    public List<FileResponse> getAttachments(Long taskId) {
        return fileRepository.findAllByTaskId(taskId).stream()
                .map(fileMapper::toDto)
                .collect(Collectors.toList());
    }

    public File getFileMetadataEntity(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public List<String> getFiles() {
        return fileRepository.findAll().stream()
                .map(File::getOriginalFilename)
                .collect(Collectors.toList());
    }

    public byte[] getFileContent(Long fileId) {
        File file = getFileMetadataEntity(fileId);
        return fileStorage.getFileContent(file.getUuid());
    }
}
