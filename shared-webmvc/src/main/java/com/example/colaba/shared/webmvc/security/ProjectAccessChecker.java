package com.example.colaba.shared.webmvc.security;

import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.webmvc.circuit.ProjectServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectAccessChecker {

    private final ProjectServiceClientWrapper client;

    public boolean isOwner(Long projectId, Long userId) {
        return client.isOwner(projectId, userId);
    }

    public boolean isAtLeastEditor(Long projectId, Long userId) {
        return client.isAtLeastEditor(projectId, userId);
    }

    public boolean hasAnyRole(Long projectId, Long userId) {
        return client.hasAnyRole(projectId, userId);
    }

    public ProjectRole getUserProjectRole(Long projectId, Long userId) {
        String roleName = client.getUserProjectRole(projectId, userId);
        return roleName == null ? null : ProjectRole.valueOf(roleName);
    }

    public void requireOwner(Long projectId, Long currentUserId) {
        if (!isOwner(projectId, currentUserId)) {
            throw new AccessDeniedException("Only the project OWNER can perform this action");
        }
    }

    public void requireAtLeastEditor(Long projectId, Long currentUserId) {
        if (!isAtLeastEditor(projectId, currentUserId)) {
            throw new AccessDeniedException(
                    "Required project role: at least EDITOR. Current role: " +
                            getUserProjectRole(projectId, currentUserId));
        }
    }

    public void requireAnyRole(Long projectId, Long currentUserId) {
        if (!hasAnyRole(projectId, currentUserId)) {
            throw new AccessDeniedException("You must be a member of this project to perform this action");
        }
    }
}
