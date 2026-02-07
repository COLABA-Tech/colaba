package com.example.colaba.file.repository;

import com.example.colaba.file.entity.FileJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileJpa, Long> {
}