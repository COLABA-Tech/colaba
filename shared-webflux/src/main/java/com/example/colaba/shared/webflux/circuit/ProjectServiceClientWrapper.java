package com.example.colaba.shared.webflux.circuit;

import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webflux.client.ProjectServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectServiceClientWrapper {

    private final ProjectServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("project-service");
    }

    public List<ProjectResponse> findByOwnerId(Long ownerId) {
        return cb().executeSupplier(() -> client.findByOwnerId(ownerId).collectList().block());
    }

    public void deleteProject(Long projectId) {
        cb().executeRunnable(() -> client.deleteProject(projectId).block());
    }

    public boolean projectExists(Long projectId) {
        return cb().executeSupplier(() -> client.projectExists(projectId).block());
    }

    public void handleUserDeletion(Long userId) {
        cb().executeRunnable(() -> client.handleUserDeletion(userId).block());
    }

    public boolean isMember(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.isMember(projectId, userId).block());
    }

    public TagResponse getTagById(Long tagId) {
        return cb().executeSupplier(() -> client.getTagById(tagId).block());
    }

    public List<TagResponse> getTagsByIds(List<Long> tagIds) {
        return cb().executeSupplier(() -> client.getTagsByIds(tagIds).collectList().block());
    }

    public boolean tagExists(Long tagId) {
        return cb().executeSupplier(() -> client.tagExists(tagId).block());
    }

    public boolean hasAnyRole(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.hasAnyRole(projectId, userId).block());
    }

    public boolean isAtLeastEditor(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.isAtLeastEditor(projectId, userId).block());
    }

    public boolean isOwner(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.isOwner(projectId, userId).block());
    }

    public String getUserProjectRole(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.getUserProjectRole(projectId, userId).block());
    }
}
