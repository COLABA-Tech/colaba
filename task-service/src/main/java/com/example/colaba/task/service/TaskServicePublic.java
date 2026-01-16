package com.example.colaba.task.service;

import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webmvc.security.ProjectAccessChecker;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.entity.task.TaskJpa;
import com.example.colaba.task.repository.TaskRepository;
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
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public Page<TaskResponse> getAllTasks(Pageable pageable, Long currentUserId) {
        return taskService.getAllTasks(pageable);
    }

    public TaskResponse getTaskById(Long id, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(id);
        accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        return taskService.getTaskById(id);
    }

    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable, Long currentUserId) {
        accessChecker.requireAnyRole(projectId, currentUserId);
        return taskService.getTasksByProject(projectId, pageable);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long currentUserId) {
        accessChecker.requireAtLeastEditor(request.projectId(), currentUserId);
        return taskService.createTask(request);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(id);
        accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        return taskService.updateTask(id, request);
    }

    @Transactional
    public void deleteTask(Long id, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(id);
        accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        taskService.deleteTask(id);
    }

    public Page<TaskResponse> getTasksByAssignee(Long assigneeId, Pageable pageable, Long currentUserId) {
        if (!assigneeId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only view your own assigned tasks");
        }
        return taskService.getTasksByAssignee(assigneeId, pageable);
    }

    public List<TagResponse> getTagsByTask(Long taskId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        return taskService.getTagsByTask(taskId);
    }

    @Transactional
    public void assignTagToTask(Long taskId, Long tagId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        taskService.assignTagToTask(taskId, tagId);
    }

    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        taskService.removeTagFromTask(taskId, tagId);
    }
}
