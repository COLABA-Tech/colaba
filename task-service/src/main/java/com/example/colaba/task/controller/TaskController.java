package com.example.colaba.task.controller;

import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks Public", description = "API for managing tasks")
public class TaskController extends BaseController {
    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Get all tasks with pagination", description = "Retrieves a paginated list of all tasks. Supports standard Spring Pageable parameters (page, size, sort).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks")
    })
    public ResponseEntity<Page<TaskResponse>> getAllTasks(Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project ID with pagination", description = "Retrieves a paginated list of tasks for a specific project. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks for the project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Page<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId, Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assignee/{userId}")
    @Operation(summary = "Get tasks by assignee ID with pagination", description = "Retrieves a paginated list of tasks assigned to a specific user. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks for the assignee"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<TaskResponse>> getTasksByAssignee(@PathVariable Long userId, Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByAssignee(userId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Partially updates a task by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}