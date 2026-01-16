package com.example.colaba.project.repository;

import com.example.colaba.project.entity.TagJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagJpa, Long> {
    Page<TagJpa> findByProjectId(Long projectId, Pageable pageable);

    Optional<TagJpa> findByProjectIdAndNameIgnoreCase(Long projectId, String name);

    @Modifying
    @Query("DELETE FROM TagJpa t WHERE t.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT t FROM TagJpa t JOIN ProjectMemberJpa pm ON t.projectId = pm.projectId WHERE pm.userId = :userId")
    Page<TagJpa> findByUserId(@Param("userId") Long userId, Pageable pageable);
}