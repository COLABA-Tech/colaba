package com.example.colaba.project.repository;

import com.example.colaba.project.entity.ProjectJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectJpa, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<ProjectJpa> findByOwnerId(Long ownerId);
}
