package com.example.colaba.shared.webmvc.application.security;

import com.example.colaba.shared.common.domain.entity.ProjectRole;

public interface ProjectAccessService {
    boolean isOwner(Long projectId, Long userId);

    boolean isAtLeastEditor(Long projectId, Long userId);

    boolean hasAnyRole(Long projectId, Long userId);

    ProjectRole getUserProjectRole(Long projectId, Long userId);

    void requireOwner(Long projectId, Long currentUserId);

    void requireAtLeastEditor(Long projectId, Long currentUserId);

    void requireAnyRole(Long projectId, Long currentUserId);
}
