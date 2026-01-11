package com.example.colaba.task.controller;

import com.example.colaba.task.repository.TaskRepository;
import com.example.colaba.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks/internal")
@RequiredArgsConstructor
public class TaskInternalController {
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    @DeleteMapping("/project/{projectId}")
    public void deleteTasksByProject(@PathVariable Long projectId) {
        taskService.deleteTasksByProject(projectId);
    }

    @PostMapping("/user/{userId}/deletion")
    public void handleUserDeletion(@PathVariable Long userId) {
        taskService.handleUserDeletion(userId);
    }

    @GetMapping("/{id}/exists")
    public boolean taskExists(@PathVariable Long id) {
        return taskRepository.existsById(id);
    }

    @DeleteMapping("/task-tags/tag/{tagId}")
    void deleteTaskTagsByTagId(@PathVariable Long tagId) {
        taskService.deleteTaskTagsByTagId(tagId);
    }
}