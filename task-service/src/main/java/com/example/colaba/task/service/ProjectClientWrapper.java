package com.example.colaba.task.service;

import com.example.colaba.shared.client.ProjectServiceClient;
import com.example.colaba.shared.entity.Project;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}