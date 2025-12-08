package com.example.colaba.shared.client;

import com.example.colaba.shared.entity.task.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@FeignClient(
        name = "task-service",
        path = "/api/tasks"
)
public interface TaskServiceClient {
    @GetMapping("/entity/{id}")
    Mono<Task> getTaskEntityById(@PathVariable Long id);

    @PutMapping("/{id}")
    Mono<Task> updateTask(@PathVariable Long id, @RequestBody Task task);
}
