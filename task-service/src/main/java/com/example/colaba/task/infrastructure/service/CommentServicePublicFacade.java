package com.example.colaba.task.infrastructure.service;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.task.application.dto.comment.CommentResponse;
import com.example.colaba.task.application.dto.comment.CreateCommentRequest;
import com.example.colaba.task.application.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.application.service.CommentServicePublic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServicePublicFacade {
    private final CommentServicePublic commentServicePublic;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request, Long currentUserId) {
        return commentServicePublic.createComment(request, currentUserId);
    }

    @Transactional
    public CommentResponse getCommentById(Long id, Long currentUserId) {
        return commentServicePublic.getCommentById(id, currentUserId);
    }

    @Transactional
    public PagedResult<CommentResponse> getCommentsByTask(Long taskId, PaginationRequest pageable, Long currentUserId) {
        return commentServicePublic.getCommentsByTask(taskId, pageable, currentUserId);
    }

    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request, Long currentUserId) {
        return commentServicePublic.updateComment(id, request, currentUserId);
    }

    @Transactional
    public void deleteComment(Long id, Long currentUserId) {
        commentServicePublic.deleteComment(id, currentUserId);
    }
}
