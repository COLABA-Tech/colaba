package com.example.colaba.task.service;

import com.example.colaba.shared.webmvc.security.ProjectAccessChecker;
import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.dto.comment.CommentScrollResponse;
import com.example.colaba.task.dto.comment.CreateCommentRequest;
import com.example.colaba.task.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.entity.CommentJpa;
import com.example.colaba.task.entity.task.TaskJpa;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServicePublic {
    private final ProjectAccessChecker accessChecker;
    private final CommentService commentService;
    private final TaskService taskService;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(request.taskId());
        accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        if (!request.userId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only create comments on your own behalf");
        }
        return commentService.createComment(request);
    }

    public CommentResponse getCommentById(Long id, Long currentUserId) {
//        CommentJpa comment = commentService.getCommentEntityById(id);
//        accessChecker.requireAnyRole(comment.getTask().getProjectId(), currentUserId);
        return commentService.getCommentById(id);
    }

    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        return commentService.getCommentsByTask(taskId, pageable);
    }

    public CommentScrollResponse getCommentsByTaskScroll(Long taskId, String cursor, int limit, Long currentUserId) {
        TaskJpa task = taskService.getTaskEntityById(taskId);
        accessChecker.requireAnyRole(task.getProjectId(), currentUserId);
        return commentService.getCommentsByTaskScroll(taskId, cursor, limit);
    }

    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request, Long currentUserId) {
        CommentJpa comment = commentService.getCommentEntityById(id);
        if (!comment.getUserId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only update your own comments");
        }
        return commentService.updateComment(id, request);
    }

    @Transactional
    public void deleteComment(Long id, Long currentUserId) {
        CommentJpa comment = commentService.getCommentEntityById(id);
        boolean isAuthor = comment.getUserId().equals(currentUserId);
        if (!isAuthor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only delete your own comments");
        }
        commentService.deleteComment(id);
    }
}
