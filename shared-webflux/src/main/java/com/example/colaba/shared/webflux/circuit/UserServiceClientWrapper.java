package com.example.colaba.shared.webflux.circuit;

import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceClientWrapper {

    private final UserServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("user-service");
    }

    public boolean userExists(Long userId) {
        return cb().executeSupplier(() -> client.userExists(userId).block());
    }

    public boolean isAdmin(Long id) {
        return Boolean.TRUE.equals(cb().executeSupplier(() -> client.isAdmin(id)).block());
    }

    public UserRole getUserRole(Long id) {
        return cb().executeSupplier(() -> client.getUserRole(id)).block();
    }

    public boolean canManageUser(Long currentUserId, Long targetUserId) {
        return Boolean.TRUE.equals(cb().executeSupplier(() -> client.canManageUser(currentUserId, targetUserId)).block());
    }
}