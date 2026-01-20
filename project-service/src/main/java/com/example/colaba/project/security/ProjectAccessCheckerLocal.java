package com.example.colaba.project.security;

import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.shared.common.entity.ProjectRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProjectAccessCheckerLocal {

    private final ProjectMemberRepository memberRepository;

    public boolean isOwner(Long projectId, Long userId) {
        return memberRepository.existsByProjectIdAndUserIdAndRole(
                projectId, userId, ProjectRole.OWNER);
    }

    public boolean isAtLeastEditor(Long projectId, Long userId) {
        return memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                projectId, userId, Set.of(ProjectRole.EDITOR, ProjectRole.OWNER));
    }

    public boolean hasAnyRole(Long projectId, Long userId) {
        return memberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public ProjectRole getUserProjectRole(Long projectId, Long userId) {
        return memberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(ProjectMemberJpa::getRole)
                .orElse(null);
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

    // === Реактивные аналоги ===
    public Mono<Boolean> isOwnerMono(Long projectId, Long userId) {
        return Mono.fromCallable(() -> isOwner(projectId, userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> isAtLeastEditorMono(Long projectId, Long userId) {
        return Mono.fromCallable(() -> isAtLeastEditor(projectId, userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> hasAnyRoleMono(Long projectId, Long userId) {
        return Mono.fromCallable(() -> hasAnyRole(projectId, userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectRole> getUserProjectRoleMono(Long projectId, Long userId) {
        return Mono.fromCallable(() -> getUserProjectRole(projectId, userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> requireOwnerMono(Long projectId, Long userId) {
        return isOwnerMono(projectId, userId)
                .flatMap(isOwner -> {
                    if (isOwner) {
                        return Mono.empty();
                    }
                    return Mono.error(new AccessDeniedException("Only the project OWNER can perform this action"));
                });
    }

    public Mono<Void> requireAtLeastEditorMono(Long projectId, Long userId) {
        return isAtLeastEditorMono(projectId, userId)
                .flatMap(isEditor -> {
                    if (isEditor) {
                        return Mono.empty();
                    }
                    return getUserProjectRoleMono(projectId, userId)
                            .map(role -> "Current role: " + (role == null ? "none" : role))
                            .defaultIfEmpty("Current role: none")
                            .flatMap(message -> Mono.error(new AccessDeniedException(
                                    "Required project role: at least EDITOR. " + message)));
                });
    }

    public Mono<Void> requireAnyRoleMono(Long projectId, Long userId) {
        return hasAnyRoleMono(projectId, userId)
                .flatMap(hasRole -> {
                    if (hasRole) {
                        return Mono.empty();
                    }
                    return Mono.error(new AccessDeniedException("You must be a member of this project to perform this action"));
                });
    }
}