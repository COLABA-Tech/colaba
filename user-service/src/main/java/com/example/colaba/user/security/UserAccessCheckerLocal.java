package com.example.colaba.user.security;

import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserAccessCheckerLocal {
    private final UserRepository userRepository;

    public Mono<Boolean> isAdminMono(Long userId) {
        return userRepository.existsByIdAndRole(userId, UserRole.ADMIN);
    }

    public Mono<UserRole> getUserRoleMono(Long userId) {
        return userRepository.findRoleById(userId);
    }

    public Mono<Void> requireAdminMono(Long userId) {
        return isAdminMono(userId)
                .flatMap(isAdmin -> {
                    if (Boolean.TRUE.equals(isAdmin)) {
                        return Mono.just(new Object());
                    }
                    return Mono.error(new AccessDeniedException("Required user role: ADMIN"));
                })
                .switchIfEmpty(Mono.error(new AccessDeniedException("User not found")))
                .then();
    }

    public Mono<Boolean> canManageUserMono(Long currentUserId, Long targetUserId) {
        return Mono.just(currentUserId.equals(targetUserId))
                .flatMap(canManage -> {
                    if (canManage) {
                        return Mono.just(true);
                    }
                    return isAdminMono(currentUserId);
                });
    }

    public Mono<Void> requireCanManageUserMono(Long currentUserId, Long targetUserId) {
        return canManageUserMono(currentUserId, targetUserId)
                .flatMap(canManage -> {
                    if (!canManage) {
                        return Mono.error(new AccessDeniedException("You can only manage your own account or as ADMIN"));
                    }
                    return Mono.empty();
                });
    }

    public boolean isAdmin(Long userId) {
        return Boolean.TRUE.equals(isAdminMono(userId).block());
    }

    public UserRole getUserRole(Long userId) {
        return getUserRoleMono(userId).block();
    }

    public void requireAdmin(Long currentUserId) {
        if (!isAdmin(currentUserId)) {
            throw new AccessDeniedException(
                    "Required user role: ADMIN. Current role: " +
                            getUserRole(currentUserId));
        }
    }

    public boolean canManageUser(Long currentUserId, Long targetUserId) {
        return currentUserId.equals(targetUserId) || isAdmin(currentUserId);
    }

    public void requireCanManageUser(Long currentUserId, Long targetUserId) {
        if (!canManageUser(currentUserId, targetUserId)) {
            throw new AccessDeniedException("You can only manage your own account or as ADMIN");
        }
    }
}
