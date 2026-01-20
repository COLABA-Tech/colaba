package com.example.colaba.shared.webflux.circuit;

import com.example.colaba.shared.webflux.client.TaskServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TaskServiceClientWrapper {

    private final TaskServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("task-service");
    }

    public Mono<Void> deleteTasksByProject(Long projectId) {
        return client.deleteTasksByProject(projectId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Void> handleUserDeletion(Long userId) {
        return client.handleUserDeletion(userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> taskExists(Long taskId) {
        return client.taskExists(taskId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Void> deleteTaskTagsByTagId(Long tagId) {
        return client.deleteTaskTagsByTagId(tagId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }
}
