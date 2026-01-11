package com.example.colaba.project.circuit;

import com.example.colaba.project.client.TaskServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskServiceClientWrapper {

    private final TaskServiceClient client;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("task-service");
    }

    public void deleteTasksByProject(Long projectId) {
        cb().executeRunnable(() -> client.deleteTasksByProject(projectId));
    }

    public void handleUserDeletion(Long userId) {
        cb().executeRunnable(() -> client.handleUserDeletion(userId));
    }

    public boolean taskExists(Long taskId) {
        return cb().executeSupplier(() -> client.taskExists(taskId));
    }

    public void deleteTaskTagsByTagId(Long tagId) {
        cb().executeRunnable(() -> client.deleteTaskTagsByTagId(tagId));
    }
}
