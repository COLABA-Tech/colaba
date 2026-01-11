package com.example.colaba.project.controller;

import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.dto.project.ProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/internal")
@RequiredArgsConstructor
public class ProjectInternalController {

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    @GetMapping("/owner/{ownerId}")
    public List<ProjectResponse> findByOwnerId(@PathVariable Long ownerId) {
        return projectService.getProjectByOwnerId(ownerId).block();
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }

    @GetMapping("/{id}/exists")
    public boolean projectExists(@PathVariable Long id) {
        return projectRepository.existsById(id);
    }

    @DeleteMapping("/user/{userId}/memberships")
    public void handleUserDeletion(@PathVariable Long userId) {
        projectService.handleUserDeletion(userId);
    }

    @GetMapping("/{projectId}/membership/{userId}")
    public boolean isMember(@PathVariable Long projectId, @PathVariable Long userId) {
        return Boolean.TRUE.equals(projectService.isMember(projectId, userId).block());
    }
}
