package com.example.colaba.task.application.service;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.shared.common.application.dto.file.FileDto;
import com.example.colaba.shared.common.application.dto.tag.TagResponse;
import com.example.colaba.shared.common.domain.exception.common.AccessDeniedException;
import com.example.colaba.shared.webmvc.application.ports.FileServicePort;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.shared.webmvc.application.security.ProjectAccessService;
import com.example.colaba.task.application.dto.task.CreateTaskRequest;
import com.example.colaba.task.application.dto.task.TaskResponse;
import com.example.colaba.task.application.dto.task.UpdateTaskRequest;
import com.example.colaba.task.domain.entity.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
public class TaskServicePublic {
    private final ProjectAccessService accessChecker;
    private final TaskService taskService;
    private final UserServicePort userServiceClient;
    private final FileServicePort fileServiceClient;

    public PagedResult<TaskResponse> getAllTasks(PaginationRequest PaginationRequest, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (isAdmin) {
            return taskService.getAllTasks(PaginationRequest);
        }
        throw new AccessDeniedException("Required user role: ADMIN");
    }

    public TaskResponse getTaskById(Long id, Long currentUserId) {
        Task task = taskService.getTaskEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return taskService.getTaskById(id);
    }

    public PagedResult<TaskResponse> getTasksByProject(Long projectId, PaginationRequest PaginationRequest, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(projectId, currentUserId);
        }
        return taskService.getTasksByProject(projectId, PaginationRequest);
    }

    public TaskResponse createTask(CreateTaskRequest request, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(request.projectId(), currentUserId);
        }
        return taskService.createTask(request, currentUserId);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Long currentUserId) {
        Task task = taskService.getTaskEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        return taskService.updateTask(id, request);
    }

    public void deleteTask(Long id, Long currentUserId) {
        Task task = taskService.getTaskEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        taskService.deleteTask(id);
    }

    public PagedResult<TaskResponse> getTasksByAssignee(Long assigneeId, PaginationRequest PaginationRequest, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin && !assigneeId.equals(currentUserId)) {
            throw new AccessDeniedException("You can only view your own assigned tasks");
        }
        return taskService.getTasksByAssignee(assigneeId, PaginationRequest);
    }

    public List<TagResponse> getTagsByTask(Long taskId, Long currentUserId) {
        Task task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return taskService.getTagsByTask(taskId);
    }

    public void assignTagToTask(Long taskId, Long tagId, Long currentUserId) {
        Task task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        taskService.assignTagToTask(taskId, tagId);
    }

    public void removeTagFromTask(Long taskId, Long tagId, Long currentUserId) {
        Task task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAtLeastEditor(task.getProjectId(), currentUserId);
        }
        taskService.removeTagFromTask(taskId, tagId);
    }

    public List<FileDto> getTaskAttachments(Long taskId, Long currentUserId) {
        Task task = taskService.getTaskEntityById(taskId);
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
        Task task = taskService.getTaskEntityById(taskId);
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
        Task task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return fileServiceClient.downloadFile(fileId);
    }
}
