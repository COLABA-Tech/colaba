package com.example.colaba.shared.circuit;

import com.example.colaba.shared.client.ProjectServiceClient;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectClientWrapper {

    private final ProjectServiceClient projectServiceClient;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("projectClient");
    }

    public Project getProjectEntityById(Long id) {
        return cb().executeSupplier(() -> projectServiceClient.getProjectEntityById(id));
    }

    public List<Project> findByOwner(UserJpa owner) {
        return cb().executeSupplier(() -> projectServiceClient.findByOwner(owner));
    }

    public void deleteProject(Long id) {
        cb().executeRunnable(() -> projectServiceClient.deleteProject(id));
    }
}