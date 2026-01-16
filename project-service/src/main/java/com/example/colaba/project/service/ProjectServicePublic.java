package com.example.colaba.project.service;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.ProjectScrollResponse;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServicePublic {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectAccessCheckerLocal projectAccessCheckerLocal;
    private final ProjectService projectService;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Mono<ProjectResponse> createProject(CreateProjectRequest request, Long currentUserId) {
        return Mono.defer(() -> {
            if (request.ownerId() == null ||
                    !request.ownerId().equals(currentUserId)) {
                return Mono.error(
                        new AccessDeniedException("You can only create projects for yourself")
                );
            }
            return projectService.createProject(request);
        });
    }

    public Mono<ProjectResponse> getProjectById(Long id, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return projectService.getProjectById(id);
                    }
                    return projectAccessCheckerLocal
                            .requireAnyRoleMono(id, currentUserId)
                            .flatMap(v -> projectService.getProjectById(id));
                });
    }

    public Mono<Page<ProjectResponse>> getAllProjects(Pageable pageable, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Mono.fromCallable(() -> {
                            Page<ProjectJpa> allProjects = projectRepository.findAll(pageable);
                            return projectMapper.toProjectResponsePage(allProjects);
                        }).subscribeOn(Schedulers.boundedElastic());
                    }
                    return Mono.fromCallable(() -> {
                        Page<ProjectJpa> userProjects = projectRepository.findUserProjects(currentUserId, pageable);
                        return projectMapper.toProjectResponsePage(userProjects);
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Transactional
    public Mono<ProjectResponse> updateProject(Long id, UpdateProjectRequest request, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return projectService.updateProject(id, request);
                    }
                    return projectAccessCheckerLocal
                            .requireOwnerMono(id, currentUserId)
                            .flatMap(v -> projectService.updateProject(id, request));
                });
    }

    @Transactional
    public Mono<ProjectResponse> changeProjectOwner(Long projectId, Long newOwnerId, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return projectService.changeProjectOwner(projectId, newOwnerId);
                    }
                    return projectAccessCheckerLocal
                            .requireOwnerMono(projectId, currentUserId)
                            .flatMap(v -> projectService.changeProjectOwner(projectId, newOwnerId));
                });
    }

    @Transactional
    public Mono<Void> deleteProject(Long id, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return Mono.fromRunnable(() -> projectService.deleteProject(id));
                    }
                    return projectAccessCheckerLocal.requireOwnerMono(id, currentUserId)
                            .then(Mono.fromRunnable(() -> projectService.deleteProject(id)));
                });
    }

    public Mono<List<ProjectResponse>> getProjectByOwnerId(Long ownerId, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return projectService.getProjectByOwnerId(ownerId);
                    }
                    if (!ownerId.equals(currentUserId)) {
                        return Mono.error(
                                new AccessDeniedException("You can only view your own projects")
                        );
                    }
                    return projectService.getProjectByOwnerId(ownerId);
                });
    }

    public Mono<ProjectScrollResponse> scroll(int page, int size, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> Mono.fromCallable(() -> {
                    Pageable pageable = PageRequest.of(page, size);
                    Page<ProjectJpa> projectPage;

                    if (isAdmin) {
                        projectPage = projectRepository.findAll(pageable);
                    } else {
                        projectPage = projectRepository.findUserProjects(currentUserId, pageable);
                    }

                    List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
                    boolean hasNext = projectPage.hasNext();
                    long total = projectPage.getTotalElements();

                    return new ProjectScrollResponse(projects, hasNext, total);
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}
