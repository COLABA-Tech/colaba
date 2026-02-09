package com.example.colaba.task.application.service;

import com.example.colaba.shared.common.domain.exception.comment.CommentNotFoundException;
import com.example.colaba.shared.common.domain.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.domain.exception.user.UserNotFoundException;
import com.example.colaba.task.application.dto.comment.CommentResponse;
import com.example.colaba.task.application.dto.comment.CommentScrollResponse;
import com.example.colaba.task.application.dto.comment.CreateCommentRequest;
import com.example.colaba.task.application.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.application.ports.CommentRepositoryPort;
import com.example.colaba.task.application.ports.TaskRepositoryPort;
import com.example.colaba.task.application.ports.UserServicePort;
import com.example.colaba.task.domain.entity.Comment;
import com.example.colaba.task.infrastructure.persistence.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepositoryPort commentRepository;
    private final TaskRepositoryPort taskRepository;
    private final UserServicePort userServiceClient;
    private final CommentMapper commentMapper;

    @Transactional
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

    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        Page<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return commentMapper.toResponsePage(comments);
    }

    public CommentScrollResponse getCommentsByTaskScroll(Long taskId, String cursor, int limit) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }

        OffsetDateTime cursorTime;
        if (cursor == null || cursor.isBlank()) {
            cursorTime = OffsetDateTime.now();
        } else {
            try {
                cursorTime = OffsetDateTime.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Invalid cursor format. Expected ISO-8601 OffsetDateTime, e.g. 2025-01-20T18:54:56Z"
                );
            }
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Slice<Comment> slice = commentRepository
                .findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(taskId, cursorTime, pageable);

        List<CommentResponse> responses = commentMapper.toResponseList(slice.getContent());

        String nextCursor = slice.hasContent()
                ? slice.getContent().get(slice.getContent().size() - 1).getCreatedAt().toString()
                : null;

        return new CommentScrollResponse(responses, nextCursor, slice.hasNext());
    }

    @Transactional
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

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException(id);
        }
        commentRepository.deleteById(id);
    }
}