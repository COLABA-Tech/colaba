package com.example.colaba.task.service;

import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webmvc.client.UserServiceClient;
import com.example.colaba.shared.webmvc.security.ProjectAccessChecker;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.entity.task.TaskJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServicePublic {
    private final ProjectAccessChecker accessChecker;
    private final TaskService taskService;
    private final UserServiceClient userServiceClient;

    public Page<TaskResponse> getAllTasks(Pageable pageable, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (isAdmin) {
            return taskService.getAllTasks(pageable);
        }
        throw new AccessDeniedException("Required user role: ADMIN");
    }

    public TaskResponse getTaskById(Long id, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return taskService.getTaskById(id);
    }

    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(projectId, currentUserId);
        }
        return taskService.getTasksByProject(projectId, pageable);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(request.projectId(), currentUserId);
        }
        return taskService.createTask(request, currentUserId);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        return taskService.updateTask(id, request);
    }

    @Transactional
    public void deleteTask(Long id, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        taskService.deleteTask(id);

    }

    public Page<TaskResponse> getTasksByAssignee(Long assigneeId, Pageable pageable, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin && !assigneeId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only view your own assigned tasks");
        }
        return taskService.getTasksByAssignee(assigneeId, pageable);
    }

    public List<TagResponse> getTagsByTask(Long taskId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return taskService.getTagsByTask(taskId);

    }

    @Transactional
    public void assignTagToTask(Long taskId, Long tagId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        taskService.assignTagToTask(taskId, tagId);

    }

    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        taskService.removeTagFromTask(taskId, tagId);

    }
}
