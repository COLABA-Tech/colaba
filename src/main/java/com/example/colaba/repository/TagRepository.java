package com.example.colaba.repository;

import com.example.colaba.entity.Project;
import com.example.colaba.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Page<Tag> findByProject(Project project, Pageable pageable);

    @Query("SELECT t FROM Tag t JOIN t.tasks ts WHERE ts.id = :taskId")
    List<Tag> findByTaskId(@Param("taskId") Long taskId);

    Optional<Tag> findByProjectIdAndNameIgnoreCase(Long projectId, String name);
}