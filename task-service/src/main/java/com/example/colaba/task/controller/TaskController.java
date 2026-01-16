package com.example.colaba.task.controller;

import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.service.TaskServicePublic;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks Public", description = "API for managing tasks")
public class TaskController extends BaseController {
    private final TaskServicePublic taskService;

    @GetMapping
    @Operation(summary = "Get all tasks with pagination", description = "Retrieves a paginated list of all tasks from projects where user is a member. Supports standard Spring Pageable parameters (page, size, sort).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks"),
            @ApiResponse(responseCode = "403", description = "User not authenticated")
    })
    public ResponseEntity<Page<TaskResponse>> getAllTasks(
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable, currentUserId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves a specific task by its ID. User must be a member of the task's project.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "User doesn't have access to the task")
    })
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        TaskResponse task = taskService.getTaskById(id, currentUserId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project ID with pagination", description = "Retrieves a paginated list of tasks for a specific project. User must be a member of the project. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks for the project"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "User doesn't have access to the project")
    })
    public ResponseEntity<Page<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId,
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, pageable, currentUserId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/assignee/{userId}")
    @Operation(summary = "Get tasks by assignee ID with pagination", description = "Retrieves a paginated list of tasks assigned to a specific user. User can only view their own assigned tasks unless they are admin. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks for the assignee"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "User can only view their own assigned tasks")
    })
    public ResponseEntity<Page<TaskResponse>> getTasksByAssignee(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByAssignee(userId, pageable, currentUserId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/me/assigned")
    @Operation(summary = "Get tasks assigned to current user", description = "Retrieves a paginated list of tasks assigned to the current authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tasks assigned to current user")
    })
    public ResponseEntity<Page<TaskResponse>> getMyAssignedTasks(
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        Page<TaskResponse> tasks = taskService.getTasksByAssignee(currentUserId, pageable, currentUserId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details. User must be at least EDITOR in the project.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to create tasks in this project")
    })
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        TaskResponse task = taskService.createTask(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Partially updates a task by ID. User must be at least EDITOR in the project or the task assignee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to update this task")
    })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal Long currentUserId) {

        TaskResponse task = taskService.updateTask(id, request, currentUserId);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task by ID. User must be OWNER of the project or the task assignee.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to delete this task")
    })
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {

        taskService.deleteTask(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}