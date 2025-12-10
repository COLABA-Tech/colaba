package com.example.colaba.project.repository;

import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Project> findByOwner(UserJpa owner);
}
