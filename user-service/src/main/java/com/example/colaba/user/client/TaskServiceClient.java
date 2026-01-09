package com.example.colaba.user.client;

import com.example.colaba.shared.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "task-service",
        path = "/api/tasks/internal",
        configuration = FeignConfig.class
)
public interface TaskServiceClient {
    @DeleteMapping("/project/{projectId}")
    void deleteTasksByProject(@PathVariable Long projectId);

    @PostMapping("/user/{userId}/deletion")
    void handleUserDeletion(@PathVariable Long userId);

    @GetMapping("/{id}/exists")
    boolean taskExists(@PathVariable Long id);

    @DeleteMapping("/task-tags/tag/{tagId}")
    void deleteTaskTagsByTagId(@PathVariable Long tagId);
}
