package com.example.colaba.project.repository;

import com.example.colaba.project.entity.projectmember.ProjectMemberId;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberJpa, ProjectMemberId> {
    Page<ProjectMemberJpa> findByProjectId(Long projectId, Pageable pageable);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Modifying
    @Query("DELETE FROM ProjectMemberJpa pm WHERE pm.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Modifying
    @Query("DELETE FROM ProjectMemberJpa pm WHERE pm.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
