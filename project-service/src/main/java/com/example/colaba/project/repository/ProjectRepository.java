package com.example.colaba.project.repository;

import com.example.colaba.project.entity.ProjectJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectJpa, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<ProjectJpa> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM ProjectJpa p JOIN ProjectMemberJpa pm ON p.id = pm.projectId WHERE pm.userId = :userId")
    Page<ProjectJpa> findUserProjects(@Param("userId") Long userId, Pageable pageable);
}
