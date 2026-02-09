package com.example.colaba.task.infrastructure.service;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.shared.common.application.dto.file.FileDto;
import com.example.colaba.shared.common.application.dto.tag.TagResponse;
import com.example.colaba.task.application.dto.task.CreateTaskRequest;
import com.example.colaba.task.application.dto.task.TaskResponse;
import com.example.colaba.task.application.dto.task.UpdateTaskRequest;
import com.example.colaba.task.application.service.TaskServicePublic;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServicePublicFacade {
    private final TaskServicePublic taskServicePublic;

    @Transactional
    public PagedResult<TaskResponse> getAllTasks(PaginationRequest paginationRequest, Long currentUserId) {
        return taskServicePublic.getAllTasks(paginationRequest, currentUserId);
    }

    @Transactional
    public TaskResponse getTaskById(Long id, Long currentUserId) {
        return taskServicePublic.getTaskById(id, currentUserId);
    }

    @Transactional
    public PagedResult<TaskResponse> getTasksByProject(Long projectId, PaginationRequest paginationRequest, Long currentUserId) {
        return taskServicePublic.getTasksByProject(projectId, paginationRequest, currentUserId);
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long currentUserId) {
        return taskServicePublic.createTask(request, currentUserId);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request, Long currentUserId) {
        return taskServicePublic.updateTask(id, request, currentUserId);
    }

    @Transactional
    public void deleteTask(Long id, Long currentUserId) {
        taskServicePublic.deleteTask(id, currentUserId);
    }

    @Transactional
    public PagedResult<TaskResponse> getTasksByAssignee(Long assigneeId, PaginationRequest paginationRequest, Long currentUserId) {
        return taskServicePublic.getTasksByAssignee(assigneeId, paginationRequest, currentUserId);
    }

    @Transactional
    public List<TagResponse> getTagsByTask(Long taskId, Long currentUserId) {
        return taskServicePublic.getTagsByTask(taskId, currentUserId);
    }

    @Transactional
    public void assignTagToTask(Long taskId, Long tagId, Long currentUserId) {
        taskServicePublic.assignTagToTask(taskId, tagId, currentUserId);
    }

    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId, Long currentUserId) {
        taskServicePublic.removeTagFromTask(taskId, tagId, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<FileDto> getTaskAttachments(Long taskId, Long currentUserId) {
        return taskServicePublic.getTaskAttachments(taskId, currentUserId);
    }

    @Transactional
    public List<FileDto> uploadTaskAttachments(Long taskId, Long currentUserId, List<MultipartFile> files) {
        return taskServicePublic.uploadTaskAttachments(taskId, currentUserId, files);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAttachment(Long taskId, Long fileId, Long currentUserId) {
        return taskServicePublic.downloadAttachment(taskId, fileId, currentUserId);
    }
}
