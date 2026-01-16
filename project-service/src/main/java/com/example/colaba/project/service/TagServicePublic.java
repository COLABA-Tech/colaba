package com.example.colaba.project.service;

import com.example.colaba.project.dto.tag.CreateTagRequest;
import com.example.colaba.project.dto.tag.UpdateTagRequest;
import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TagServicePublic {
    private final TagService tagService;
    private final TagMapper tagMapper;
    private final ProjectAccessCheckerLocal projectAccessCheckerLocal;
    private final UserServiceClient userServiceClient;

    public Mono<Page<TagResponse>> getAllTags(Pageable pageable, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (!isAdmin) {
                        return Mono.error(new AccessDeniedException("Only ADMIN can view all tags"));
                    }
                    return tagService.getAllTags(pageable);
                });
    }

    public Mono<TagResponse> getTagById(Long id, Long currentUserId) {
        return tagService.getTagEntityById(id)
                .flatMap(tag -> userServiceClient.isAdmin(currentUserId)
                        .flatMap(isAdmin -> {
                            if (isAdmin) {
                                return Mono.just(tag);
                            }
                            return projectAccessCheckerLocal.requireAnyRoleMono(tag.getProjectId(), currentUserId)
                                    .thenReturn(tag);
                        }))
                .map(tagMapper::toTagResponse);
    }

    public Mono<Page<TagResponse>> getTagsByProject(Long projectId, Pageable pageable, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return tagService.getTagsByProject(projectId, pageable);
                    }
                    return projectAccessCheckerLocal.requireAnyRoleMono(projectId, currentUserId)
                            .then(Mono.defer(() -> tagService.getTagsByProject(projectId, pageable)));
                });
    }

    @Transactional
    public Mono<TagResponse> createTag(CreateTagRequest request, Long currentUserId) {
        return userServiceClient.isAdmin(currentUserId)
                .flatMap(isAdmin -> {
                    if (isAdmin) {
                        return tagService.createTag(request);
                    }
                    return projectAccessCheckerLocal.requireAtLeastEditorMono(request.projectId(), currentUserId)
                            .then(Mono.defer(() -> tagService.createTag(request)));
                });
    }

    @Transactional
    public Mono<TagResponse> updateTag(Long id, UpdateTagRequest request, Long currentUserId) {
        return tagService.getTagEntityById(id)
                .flatMap(tag -> userServiceClient.isAdmin(currentUserId)
                        .flatMap(isAdmin -> {
                            if (isAdmin) {
                                return tagService.updateTag(id, request);
                            }
                            return projectAccessCheckerLocal.requireAtLeastEditorMono(tag.getProjectId(), currentUserId)
                                    .thenReturn(tag);
                        }))
                .flatMap(tag -> tagService.updateTag(id, request));
    }

    @Transactional
    public Mono<Void> deleteTag(Long id, Long currentUserId) {
        return tagService.getTagEntityById(id)
                .flatMap(tag -> userServiceClient.isAdmin(currentUserId)
                        .flatMap(isAdmin -> {
                            if (isAdmin) {
                                return tagService.deleteTag(id);
                            }
                            return projectAccessCheckerLocal.requireAtLeastEditorMono(tag.getProjectId(), currentUserId)
                                    .thenReturn(tag);
                        }))
                .flatMap(tag -> tagService.deleteTag(id));
    }
}