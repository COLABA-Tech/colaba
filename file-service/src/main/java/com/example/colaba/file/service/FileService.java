package com.example.colaba.file.service;

import com.example.colaba.file.entity.FileJpa;
import com.example.colaba.file.repository.FileRepository;
import com.example.colaba.shared.common.dto.file.FileDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }


    @Value("${app.upload-dir:/uploads}")
    public Path uploadDir;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @Transactional
    public List<FileDto> uploadFiles(Long taskId, Long uploadedBy, List<MultipartFile> files) {
        return files.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> saveOneFile(taskId, uploadedBy, file))
                .collect(Collectors.toList());
    }

    private FileDto saveOneFile(Long taskId, Long uploadedBy, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        long size = file.getSize();

        UUID uuid = UUID.randomUUID();
        Path destination = uploadDir.resolve(uuid.toString());

        try {
            file.transferTo(destination.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла на диск " + originalFilename, e);
        }

        FileJpa entity = FileJpa.builder()
                .taskId(taskId)
                .uuid(uuid)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .size(size)
                .uploadedBy(uploadedBy)
                .build();

        FileJpa saved = fileRepository.save(entity);

        return toDto(saved);
    }

    public List<FileDto> getAttachments(Long taskId) {
        return fileRepository.findAllByTaskId(taskId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Resource getFileContent(Long fileId) {
        FileJpa file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        Path path = uploadDir.resolve(file.getUuid().toString());
        return new FileSystemResource(path);
    }

    private FileDto toDto(FileJpa entity) {
        return FileDto.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .originalFilename(entity.getOriginalFilename())
                .contentType(entity.getContentType())
                .size(entity.getSize())
                .uploadedBy(entity.getUploadedBy())
                .uploadedAt(entity.getCreatedAt())
                .build();
    }

    public FileDto getFileMetadata(Long fileId) {
        FileJpa fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with id " + fileId));

        return toDto(fileEntity);
    }
    public List<String> getFiles() {
        return fileRepository.findAll()
                .stream()
                .map(FileJpa::getOriginalFilename)
                .collect(Collectors.toList());
    }

}