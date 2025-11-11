package com.example.colaba.service;

import com.example.colaba.entity.Project;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
    private final UserService userService;

    // always returns stub project with id = 1
    private Project getStubProject(Long id) {
        Project project = new Project();
        project.setId(id);
        project.setName("Default Project");
        project.setDescription("Default project description");
        project.setOwner(userService.getUserEntityById(1L));
        project.setCreatedAt(LocalDateTime.now().minusDays(1));
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    public Project getProjectEntityById(Long id) {
        return getStubProject(id);
    }
}
