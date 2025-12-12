package com.example.colaba.shared.circuit;

import com.example.colaba.shared.client.TaskServiceClient;
import com.example.colaba.shared.entity.task.Task;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskClientWrapper {

    private final TaskServiceClient taskServiceClient;
    private final CircuitBreakerRegistry registry;

    private CircuitBreaker cb() {
        return registry.circuitBreaker("userClient");
    }

    public Task getTaskEntityById(Long id) {
        return cb().executeSupplier(() -> taskServiceClient.getTaskEntityById(id));
    }

    public Task updateTask(Long id, Task task) {
        return cb().executeSupplier(() -> taskServiceClient.updateTask(id, task));
    }
}