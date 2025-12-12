package com.example.colaba.project.controller;

import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/internal")
@RequiredArgsConstructor
public class ProjectInternalController {

    private final ProjectRepository projectRepository;

    @PostMapping("/owner/{ownerId}")
    public List<Project> findByOwnerId(@PathVariable Long ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    @DeleteMapping("/all")
    public void deleteAll() {
        projectRepository.deleteAll();
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping("/entity/{id}")
    public Project getProjectEntityById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }
}
