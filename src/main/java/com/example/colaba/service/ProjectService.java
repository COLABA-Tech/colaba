package com.example.colaba.service;

import com.example.colaba.entity.Project;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class ProjectService {
    @Autowired
    private UserService userService;

    // always returns stub project with id = 1
    private Project getStubProject() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Default Project");
        project.setDescription("Default project description");
        project.setOwner(userService.getUserEntityById(1L));
        project.setCreatedAt(LocalDateTime.now().minusDays(1));
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    public Project getProjectEntityById(Long id) {
        return getStubProject();
    }
}
