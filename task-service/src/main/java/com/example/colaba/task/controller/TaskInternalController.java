package com.example.colaba.task.controller;

import com.example.colaba.shared.entity.task.Task;
import com.example.colaba.shared.exception.task.TaskNotFoundException;
import com.example.colaba.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/tasks")
@RequiredArgsConstructor
public class TaskInternalController {

    private final TaskRepository taskRepository;

    @GetMapping("/entity/{id}")
    public Task getTaskEntityById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
        if (!task.getId().equals(id)) {
            throw new IllegalArgumentException("Task ID mismatch");
        }
        return taskRepository.save(task);
    }
}