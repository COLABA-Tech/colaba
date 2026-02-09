package com.example.colaba.task.application.service;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.shared.common.domain.exception.common.AccessDeniedException;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.shared.webmvc.application.security.ProjectAccessService;
import com.example.colaba.task.application.dto.comment.CommentResponse;
import com.example.colaba.task.application.dto.comment.CreateCommentRequest;
import com.example.colaba.task.application.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.domain.entity.Comment;
import com.example.colaba.task.domain.entity.Task;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentServicePublic {
    private final CommentService commentService;
    private final TaskService taskService;

    private final ProjectAccessService accessChecker;
    private final UserServicePort userServiceClient;

    public CommentResponse createComment(CreateCommentRequest request, Long currentUserId) {
        Task task = taskService.getTaskEntityById(request.taskId());
        accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        return commentService.createComment(request, currentUserId);
    }

    public CommentResponse getCommentById(Long id, Long currentUserId) {
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (isAdmin) {
            return commentService.getCommentById(id);
        }
        throw new AccessDeniedException("Required user role: ADMIN");
    }

    public PagedResult<CommentResponse> getCommentsByTask(Long taskId, PaginationRequest pageable, Long currentUserId) {
        Task task = taskService.getTaskEntityById(taskId);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        }
        return commentService.getCommentsByTask(taskId, pageable);

    }

    public CommentResponse updateComment(Long id, UpdateCommentRequest request, Long currentUserId) {
        Comment comment = commentService.getCommentEntityById(id);
        if (!comment.getUserId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only update your own comments");
        }
        return commentService.updateComment(id, request);
    }

    public void deleteComment(Long id, Long currentUserId) {
        Comment comment = commentService.getCommentEntityById(id);
        boolean isAdmin = userServiceClient.isAdmin(currentUserId);
        if (!isAdmin) {
            boolean isAuthor = comment.getUserId().equals(currentUserId);
            if (!isAuthor) {
                throw new AccessDeniedException(
                        "You can only delete your own comments");
            }
        }
        commentService.deleteComment(id);

    }
}
