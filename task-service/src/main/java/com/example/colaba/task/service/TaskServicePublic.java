package com.example.colaba.task.service;

import com.example.colaba.shared.common.dto.file.FileDto;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.webmvc.circuit.FileServiceClientWrapper;
import com.example.colaba.shared.webmvc.circuit.UserServiceClientWrapper;
import com.example.colaba.shared.webmvc.security.ProjectAccessChecker;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.entity.task.TaskJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServicePublic {
    private final ProjectAccessChecker accessChecker;
    private final TaskService taskService;
    private final UserServiceClientWrapper userServiceClient;
    private final FileServiceClientWrapper fileServiceClient;

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

    public List<FileDto> getTaskAttachments(Long taskId, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return fileServiceClient.getFilesByTaskId(taskId);
    }

    public List<FileDto> uploadTaskAttachments(
            Long taskId,
            Long currentUserId,
            List<MultipartFile> files
    ) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        return fileServiceClient.uploadFiles(taskId, currentUserId, files);
    }

    public ResponseEntity<Resource> downloadAttachment(
            Long taskId,
            Long fileId,
            Long currentUserId
    ) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return fileServiceClient.downloadFile(fileId);
    }
}
