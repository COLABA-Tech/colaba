package com.example.colaba.file.infrastructure.persistence.repository;

import com.example.colaba.file.infrastructure.persistence.entity.FileJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileJpa, Long> {

    List<FileJpa> findAllByTaskId(Long taskId);

    boolean existsByTaskIdAndUuid(Long taskId, UUID uuid);

    FileJpa findByUuid(UUID uuid);
}