package com.example.colaba.shared.client;

import com.example.colaba.shared.entity.task.Task;
import com.example.colaba.shared.feign.FeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "task-service",
        path = "/api/tasks/internal",
        configuration = FeignConfig.class
)
@CircuitBreaker(name = "task-service-cb")
public interface TaskServiceClient {
    @GetMapping("/entity/{id}")
    Task getTaskEntityById(@PathVariable Long id);

    @PutMapping("/{id}")
    Task updateTask(@PathVariable Long id, @RequestBody Task task);
}