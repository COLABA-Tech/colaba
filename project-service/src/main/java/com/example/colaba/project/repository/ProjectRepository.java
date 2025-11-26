package com.example.colaba.project.repository;

import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerId(Long ownerId);

    boolean existsByName(String name);

    @Query("SELECT p FROM Project p ORDER BY p.id ASC")
    boolean existsByNameAndIdNot(String name, Long id);

    List<Project> findByOwner(User owner);
}
