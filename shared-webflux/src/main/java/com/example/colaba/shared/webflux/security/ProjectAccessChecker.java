package com.example.colaba.shared.webflux.security;

import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.webflux.client.ProjectServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProjectAccessChecker {

    private final ProjectServiceClient client;

    public Mono<Boolean> isOwnerMono(Long projectId, Long userId) {
        return client.isOwner(projectId, userId);
    }

    public Mono<Boolean> isAtLeastEditorMono(Long projectId, Long userId) {
        return client.isAtLeastEditor(projectId, userId);
    }

    public Mono<Boolean> hasAnyRoleMono(Long projectId, Long userId) {
        return client.hasAnyRole(projectId, userId);
    }

    public Mono<ProjectRole> getUserProjectRoleMono(Long projectId, Long userId) {
        return client.getUserProjectRole(projectId, userId)
                .map(roleName -> roleName == null ? null : ProjectRole.valueOf(roleName));
    }

    public Mono<Void> requireOwnerMono(Long projectId, Long userId) {
        return isOwnerMono(projectId, userId)
                .flatMap(isOwner -> isOwner ? Mono.empty() : Mono.error(
                        new AccessDeniedException("Only the project OWNER can perform this action")));
    }

    public Mono<Void> requireAtLeastEditorMono(Long projectId, Long userId) {
        return isAtLeastEditorMono(projectId, userId)
                .flatMap(isEditor -> {
                    if (isEditor) return Mono.empty();
                    return getUserProjectRoleMono(projectId, userId)
                            .flatMap(role -> Mono.error(new AccessDeniedException(
                                    "Required project role: at least EDITOR. Current role: " + role)));
                });
    }

    public Mono<Void> requireAnyRoleMono(Long projectId, Long userId) {
        return hasAnyRoleMono(projectId, userId)
                .flatMap(hasRole -> hasRole ? Mono.empty() : Mono.error(
                        new AccessDeniedException("You must be a member of this project to perform this action")));
    }
}
