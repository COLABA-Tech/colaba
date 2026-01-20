package com.example.colaba.shared.webflux.circuit;

import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webflux.client.ProjectServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectServiceClientWrapper {

    private final ProjectServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("project-service");
    }

    public Mono<List<ProjectResponse>> findByOwnerId(Long ownerId) {
        return client.findByOwnerId(ownerId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Void> deleteProject(Long projectId) {
        return client.deleteProject(projectId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> projectExists(Long projectId) {
        return client.projectExists(projectId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Void> handleUserDeletion(Long userId) {
        return client.handleUserDeletion(userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> isMember(Long projectId, Long userId) {
        return client.isMember(projectId, userId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<TagResponse> getTagById(Long tagId) {
        return client.getTagById(tagId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<List<TagResponse>> getTagsByIds(List<Long> tagIds) {
        return client.getTagsByIds(tagIds)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
    }

    public Mono<Boolean> tagExists(Long tagId) {
        return client.tagExists(tagId)
                .transformDeferred(CircuitBreakerOperator.of(cb()));
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