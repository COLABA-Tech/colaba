package com.example.colaba.project.service;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.ProjectScrollResponse;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServicePublic {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectAccessCheckerLocal projectAccessCheckerLocal;
    private final ProjectService projectService;

    @Transactional
    public Mono<ProjectResponse> createProject(CreateProjectRequest request, Long currentUserId) {
        return Mono.defer(() -> {
            if (!request.ownerId().equals(currentUserId)) {
                return Mono.error(new AccessDeniedException("You can only create projects for yourself"));
            }
            return Mono.empty();
        }).then(projectService.createProject(request));
    }

    public Mono<ProjectResponse> getProjectById(Long id, Long currentUserId) {
        return projectAccessCheckerLocal.requireAnyRoleMono(id, currentUserId)
                .then(projectService.getProjectById(id));
    }

    public Mono<Page<ProjectResponse>> getAllProjects(Pageable pageable, Long currentUserId) {
        return Mono.fromCallable(() -> {
            Page<ProjectJpa> userProjects = projectRepository.findUserProjects(currentUserId, pageable);
            return projectMapper.toProjectResponsePage(userProjects);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ProjectResponse> updateProject(Long id, UpdateProjectRequest request, Long currentUserId) {
        return projectAccessCheckerLocal.requireOwnerMono(id, currentUserId)
                .then(projectService.updateProject(id, request));
    }

    @Transactional
    public Mono<ProjectResponse> changeProjectOwner(Long projectId, Long newOwnerId, Long currentUserId) {
        return projectAccessCheckerLocal.requireOwnerMono(projectId, currentUserId)
                .then(projectService.changeProjectOwner(projectId, newOwnerId));
    }

    @Transactional
    public void deleteProject(Long id, Long currentUserId) {
        projectAccessCheckerLocal.requireOwner(id, currentUserId);
        projectService.deleteProject(id);
    }

    public Mono<List<ProjectResponse>> getProjectByOwnerId(Long ownerId, Long currentUserId) {
        return Mono.defer(() -> {
            if (!ownerId.equals(currentUserId)) {
                return Mono.error(new AccessDeniedException("You can only view your own projects"));
            }
            return Mono.empty();
        }).then(projectService.getProjectByOwnerId(ownerId));
    }

    public Mono<ProjectScrollResponse> scroll(int page, int size, Long currentUserId) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProjectJpa> projectPage = projectRepository.findUserProjects(currentUserId, pageable);

            List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
            boolean hasNext = projectPage.hasNext();
            long total = projectPage.getTotalElements();

            return new ProjectScrollResponse(projects, hasNext, total);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
