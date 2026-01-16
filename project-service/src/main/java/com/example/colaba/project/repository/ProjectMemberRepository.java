package com.example.colaba.project.repository;

import com.example.colaba.project.entity.projectmember.ProjectMemberId;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.shared.common.entity.ProjectRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberJpa, ProjectMemberId> {
    Page<ProjectMemberJpa> findByProjectId(Long projectId, Pageable pageable);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    Optional<ProjectMemberJpa> findByProjectIdAndUserId(Long projectId, Long userId);

    boolean existsByProjectIdAndUserIdAndRole(Long projectId, Long userId, ProjectRole role);

    boolean existsByProjectIdAndUserIdAndRoleIn(Long projectId, Long userId, Iterable<ProjectRole> roles);

    @Modifying
    @Query("DELETE FROM ProjectMemberJpa pm WHERE pm.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Modifying
    @Query("DELETE FROM ProjectMemberJpa pm WHERE pm.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ProjectMemberJpa pm SET pm.role = :role WHERE pm.projectId = :projectId AND pm.userId = :userId")
    void updateRole(@Param("projectId") Long projectId, @Param("userId") Long userId, @Param("role") ProjectRole role);
}
