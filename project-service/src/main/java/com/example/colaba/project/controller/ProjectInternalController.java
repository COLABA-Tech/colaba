package com.example.colaba.project.controller;

import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.entity.ProjectRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects/internal")
@RequiredArgsConstructor
public class ProjectInternalController {

    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final ProjectAccessCheckerLocal projectAccessCheckerLocal;

    @GetMapping("/owner/{ownerId}")
    public Mono<List<ProjectResponse>> findByOwnerId(@PathVariable Long ownerId) {
        return projectService.getProjectByOwnerId(ownerId);
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
    public Mono<Void> handleUserDeletion(@PathVariable Long userId) {
        return projectService.handleUserDeletion(userId);
    }

    @GetMapping("/{projectId}/membership/{userId}")
    public Mono<Boolean> isMember(@PathVariable Long projectId, @PathVariable Long userId) {
        return Mono.just(projectAccessCheckerLocal.hasAnyRole(projectId, userId));
    }

    @GetMapping("/{projectId}/user/{userId}/any-role")
    public Mono<Boolean> hasAnyRole(@PathVariable Long projectId, @PathVariable Long userId) {
        return Mono.just(projectAccessCheckerLocal.hasAnyRole(projectId, userId));
    }

    @GetMapping("/{projectId}/user/{userId}/at-least-editor")
    public Mono<Boolean> isAtLeastEditor(@PathVariable Long projectId, @PathVariable Long userId) {
        return Mono.just(projectAccessCheckerLocal.isAtLeastEditor(projectId, userId));
    }

    @GetMapping("/{projectId}/user/{userId}/owner")
    public Mono<Boolean> isOwner(@PathVariable Long projectId, @PathVariable Long userId) {
        return Mono.just(projectAccessCheckerLocal.isOwner(projectId, userId));
    }

    @GetMapping("/{projectId}/user/{userId}/role")
    public Mono<String> getUserProjectRole(@PathVariable Long projectId, @PathVariable Long userId) {
        return Mono.fromCallable(() -> {
            ProjectRole role = projectAccessCheckerLocal.getUserProjectRole(projectId, userId);
            return role != null ? role.name() : null;
        });
    }
}