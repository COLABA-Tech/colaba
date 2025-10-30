package com.example.colaba.controller;

import com.example.colaba.dto.task.CreateTaskRequest;
import com.example.colaba.dto.task.TaskResponse;
import com.example.colaba.dto.task.UpdateTaskRequest;
import com.example.colaba.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController extends BaseController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAllTasks(Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId, Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<Page<TaskResponse>> getTasksByAssignee(@PathVariable Long userId, Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByAssignee(userId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}