package com.example.colaba.shared.webflux.circuit;

import com.example.colaba.shared.webflux.client.ProjectServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProjectServiceClientWrapper {

    private final ProjectServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("project-service");
    }

    public Mono<Boolean> hasAnyRole(Long projectId, Long userId) {
        return client.hasAnyRole(projectId, userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> isAtLeastEditor(Long projectId, Long userId) {
        return client.isAtLeastEditor(projectId, userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> isOwner(Long projectId, Long userId) {
        return client.isOwner(projectId, userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<String> getUserProjectRole(Long projectId, Long userId) {
        return client.getUserProjectRole(projectId, userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }
}