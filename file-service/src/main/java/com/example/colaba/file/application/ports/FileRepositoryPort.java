package com.example.colaba.file.application.ports;

import com.example.colaba.file.domain.entity.File;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepositoryPort {
    File save(File file);

    Optional<File> findById(Long id);

    Optional<File> findByUuid(UUID uuid);

    List<File> findAllByTaskId(Long taskId);

    List<File> findAll();

    void deleteById(Long id);
}