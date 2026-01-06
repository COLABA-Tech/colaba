package com.example.colaba.project.controller;

import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.shared.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/internal")
@RequiredArgsConstructor
public class ProjectInternalController {

    private final ProjectRepository projectRepository;

    @GetMapping("/owner/{ownerId}")
    public List<Project> findByOwnerId(@PathVariable Long ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
    }

    @GetMapping("/{id}/exists")
    public boolean projectExists(@PathVariable Long id) {
        return projectRepository.existsById(id);
    }
}
