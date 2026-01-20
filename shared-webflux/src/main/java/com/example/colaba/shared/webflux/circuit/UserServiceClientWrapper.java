package com.example.colaba.shared.webflux.circuit;

import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserServiceClientWrapper {

    private final UserServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("user-service");
    }

    public Mono<Boolean> userExists(Long userId) {
        return client.userExists(userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> isAdmin(Long id) {
        return client.isAdmin(id)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<UserRole> getUserRole(Long id) {
        return client.getUserRole(id)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> canManageUser(Long currentUserId, Long targetUserId) {
        return client.canManageUser(currentUserId, targetUserId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }
}