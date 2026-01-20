package com.example.colaba.project.service;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProjectMemberServicePublic {

    private final ProjectMemberService projectMemberService;
    private final ProjectAccessCheckerLocal accessChecker;
    private final UserServiceClient userServiceClient;

    public Mono<Page<ProjectMemberResponse>> getMembersByProject(
            Long projectId,
            Pageable pageable,
            Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        return projectMemberService.getMembersByProject(projectId, pageable);
                    }
                    return accessChecker.requireAnyRoleMono(projectId, currentUserId)
                            .then(projectMemberService.getMembersByProject(projectId, pageable));
                });
    }

    public Mono<ProjectMemberResponse> createMembership(
            Long projectId,
            CreateProjectMemberRequest request,
            Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        return projectMemberService.createMembership(projectId, request);
                    }
                    return accessChecker.requireOwnerMono(projectId, currentUserId)
                            .then(projectMemberService.createMembership(projectId, request));
                });
    }

    public Mono<ProjectMemberResponse> updateMembership(
            Long projectId,
            Long userId,
            UpdateProjectMemberRequest request,
            Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        return projectMemberService.updateMembership(projectId, userId, request);
                    }
                    return accessChecker.requireOwnerMono(projectId, currentUserId)
                            .then(projectMemberService.updateMembership(projectId, userId, request));
                });
    }

    public Mono<Void> deleteMembership(
            Long projectId,
            Long userId,
            Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        return projectMemberService.deleteMembership(projectId, userId);
                    }
                    return accessChecker.requireOwnerMono(projectId, currentUserId)
                            .then(projectMemberService.deleteMembership(projectId, userId));
                });
    }
}