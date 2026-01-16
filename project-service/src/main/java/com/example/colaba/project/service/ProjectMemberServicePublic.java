package com.example.colaba.project.service;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProjectMemberServicePublic {
    private final ProjectMemberService projectMemberService;
    private final ProjectAccessCheckerLocal accessChecker;

    public Mono<Page<ProjectMemberResponse>> getMembersByProject(
            Long projectId,
            Pageable pageable,
            Long currentUserId) {
        return accessChecker.requireAtLeastEditorMono(projectId, currentUserId)
                .then(projectMemberService.getMembersByProject(projectId, pageable));
    }

    @Transactional
    public Mono<ProjectMemberResponse> createMembership(
            Long projectId,
            CreateProjectMemberRequest request,
            Long currentUserId) {
        return accessChecker.requireOwnerMono(projectId, currentUserId)
                .then(projectMemberService.createMembership(projectId, request));
    }

    @Transactional
    public Mono<ProjectMemberResponse> updateMembership(
            Long projectId,
            Long userId,
            UpdateProjectMemberRequest request,
            Long currentUserId) {
        return accessChecker.requireOwnerMono(projectId, currentUserId)
                .then(projectMemberService.updateMembership(projectId, userId, request));
    }

    @Transactional
    public Mono<Void> deleteMembership(
            Long projectId,
            Long userId,
            Long currentUserId) {
        return accessChecker.requireOwnerMono(projectId, currentUserId)
                .then(projectMemberService.deleteMembership(projectId, userId));
    }
}