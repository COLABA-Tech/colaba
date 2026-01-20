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
import reactor.core.scheduler.Schedulers;

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
        return projectService.getProjectsByOwnerId(ownerId);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteProject(@PathVariable Long id) {
        return projectService.deleteProject(id);
    }

    @GetMapping("/{id}/exists")
    public Mono<Boolean> projectExists(@PathVariable Long id) {
        return Mono.fromCallable(() -> projectRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/user/{userId}/memberships")
    public Mono<Void> handleUserDeletion(@PathVariable Long userId) {
        return projectService.handleUserDeletion(userId);
    }

    @GetMapping("/{projectId}/membership/{userId}")
    public Mono<Boolean> isMember(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectAccessCheckerLocal.hasAnyRoleMono(projectId, userId);
    }

    @GetMapping("/{projectId}/user/{userId}/any-role")
    public Mono<Boolean> hasAnyRole(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectAccessCheckerLocal.hasAnyRoleMono(projectId, userId);
    }

    @GetMapping("/{projectId}/user/{userId}/at-least-editor")
    public Mono<Boolean> isAtLeastEditor(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectAccessCheckerLocal.isAtLeastEditorMono(projectId, userId);
    }

    @GetMapping("/{projectId}/user/{userId}/owner")
    public Mono<Boolean> isOwner(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectAccessCheckerLocal.isOwnerMono(projectId, userId);
    }

    @GetMapping("/{projectId}/user/{userId}/role")
    public Mono<String> getUserProjectRole(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectAccessCheckerLocal.getUserProjectRoleMono(projectId, userId)
                .map(ProjectRole::name)
                .defaultIfEmpty("");
    }
}