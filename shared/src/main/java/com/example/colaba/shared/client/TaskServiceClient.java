package com.example.colaba.shared.client;

import com.example.colaba.shared.entity.task.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "task-service",
        path = "/internal/tasks"  // Изменили с /api/tasks на /internal/tasks
)
public interface TaskServiceClient {
    @GetMapping("/entity/{id}")
    Task getTaskEntityById(@PathVariable Long id);  // Убрали Mono<>

    @PutMapping("/{id}")
    Task updateTask(@PathVariable Long id, @RequestBody Task task);  // Убрали Mono<>
}