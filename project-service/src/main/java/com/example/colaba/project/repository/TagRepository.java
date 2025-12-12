package com.example.colaba.project.repository;

import com.example.colaba.shared.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Page<Tag> findByProjectId(Long projectId, Pageable pageable);

    Optional<Tag> findByProjectIdAndNameIgnoreCase(Long projectId, String name);
}