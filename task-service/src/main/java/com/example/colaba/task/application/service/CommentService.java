package com.example.colaba.task.application.service;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.shared.common.domain.exception.comment.CommentNotFoundException;
import com.example.colaba.shared.common.domain.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.domain.exception.user.UserNotFoundException;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.task.application.dto.comment.CommentResponse;
import com.example.colaba.task.application.dto.comment.CreateCommentRequest;
import com.example.colaba.task.application.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.application.mapper.CommentMapper;
import com.example.colaba.task.application.ports.CommentRepositoryPort;
import com.example.colaba.task.application.ports.TaskRepositoryPort;
import com.example.colaba.task.domain.entity.Comment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentService {

    private final CommentRepositoryPort commentRepository;
    private final TaskRepositoryPort taskRepository;
    private final UserServicePort userServiceClient;
    private final CommentMapper commentMapper;

    public CommentResponse createComment(CreateCommentRequest request, Long userId) {
        boolean userExists = userServiceClient.userExists(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }

        if (!taskRepository.existsById(request.taskId())) {
            throw new TaskNotFoundException(request.taskId());
        }

        Comment comment = Comment.builder()
                .taskId(request.taskId())
                .userId(userId)
                .content(request.content())
                .build();

        Comment saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        return commentMapper.toResponse(comment);
    }

    public Comment getCommentEntityById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    public PagedResult<CommentResponse> getCommentsByTask(Long taskId, PaginationRequest pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        PagedResult<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return commentMapper.toResponsePage(comments);
    }

    public CommentResponse updateComment(Long id, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        boolean hasChanges = false;
        if (request.content() != null
                && !request.content().isBlank()
                && !request.content().equals(comment.getContent())) {
            comment.setContent(request.content());
            hasChanges = true;
        }

        Comment saved = hasChanges ? commentRepository.save(comment) : comment;
        return commentMapper.toResponse(saved);
    }

    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException(id);
        }
        commentRepository.deleteById(id);
    }
}