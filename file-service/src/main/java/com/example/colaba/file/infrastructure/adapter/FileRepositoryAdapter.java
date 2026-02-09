package com.example.colaba.file.infrastructure.adapter;

import com.example.colaba.file.application.ports.FileRepositoryPort;
import com.example.colaba.file.domain.entity.File;
import com.example.colaba.file.infrastructure.persistence.entity.FileJpa;
import com.example.colaba.file.infrastructure.persistence.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileRepositoryAdapter implements FileRepositoryPort {

    private final FileRepository fileRepository;

    @Override
    public File save(File file) {
        FileJpa fileJpa = toJpaEntity(file);
        FileJpa savedFileJpa = fileRepository.save(fileJpa);
        return toDomainEntity(savedFileJpa);
    }

    @Override
    public Optional<File> findById(Long id) {
        return fileRepository.findById(id).map(this::toDomainEntity);
    }

    @Override
    public Optional<File> findByUuid(UUID uuid) {
        return Optional.ofNullable(fileRepository.findByUuid(uuid)).map(this::toDomainEntity);
    }

    @Override
    public List<File> findAllByTaskId(Long taskId) {
        return fileRepository.findAllByTaskId(taskId).stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<File> findAll() {
        return fileRepository.findAll().stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        fileRepository.deleteById(id);
    }

    private FileJpa toJpaEntity(File file) {
        return FileJpa.builder()
                .id(file.getId())
                .taskId(file.getTaskId())
                .uuid(file.getUuid())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedBy(file.getUploadedBy())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private File toDomainEntity(FileJpa fileJpa) {
        return File.builder()
                .id(fileJpa.getId())
                .taskId(fileJpa.getTaskId())
                .uuid(fileJpa.getUuid())
                .originalFilename(fileJpa.getOriginalFilename())
                .contentType(fileJpa.getContentType())
                .size(fileJpa.getSize())
                .uploadedBy(fileJpa.getUploadedBy())
                .createdAt(fileJpa.getCreatedAt())
                .build();
    }
}