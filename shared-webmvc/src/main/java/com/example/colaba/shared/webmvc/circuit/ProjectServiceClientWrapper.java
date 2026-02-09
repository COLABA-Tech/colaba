package com.example.colaba.shared.webmvc.circuit;

import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webmvc.client.ProjectServiceClient;
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

    public boolean projectExists(Long projectId) {
        return cb().executeSupplier(() -> client.projectExists(projectId));
    }

    public TagResponse getTagById(Long tagId) {
        return cb().executeSupplier(() -> client.getTagById(tagId));
    }

    public List<TagResponse> getTagsByIds(List<Long> tagIds) {
        return cb().executeSupplier(() -> client.getTagsByIds(tagIds));
    }

    public boolean hasAnyRole(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.hasAnyRole(projectId, userId));
    }

    public boolean isAtLeastEditor(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.isAtLeastEditor(projectId, userId));
    }

    public boolean isOwner(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.isOwner(projectId, userId));
    }

    public String getUserProjectRole(Long projectId, Long userId) {
        return cb().executeSupplier(() -> client.getUserProjectRole(projectId, userId));
    }
}