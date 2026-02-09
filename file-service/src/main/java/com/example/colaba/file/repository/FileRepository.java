package com.example.colaba.file.repository;

import com.example.colaba.file.entity.FileJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileJpa, Long> {

    List<FileJpa> findAllByTaskId(Long taskId);

    boolean existsByTaskIdAndUuid(Long taskId, UUID uuid);

    FileJpa findByUuid(UUID uuid);
}