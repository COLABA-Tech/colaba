package com.example.colaba.repository;

import com.example.colaba.entity.Project;
import com.example.colaba.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);
    boolean existsByName(String name);

    // Пагинация с Slice
    @Query("SELECT p FROM Project p ORDER BY p.id ASC")
    Slice<Project> findAllBy(Pageable pageable);
    // Проверка на дубликат при обновлении: есть ли проект с таким именем, но другим ID
    boolean existsByNameAndIdNot(String name, Long id);
    List<Project> findByOwner(User owner);


}
