package com.example.colaba.task.service;

import com.example.colaba.shared.exception.comment.CommentNotFoundException;
import com.example.colaba.shared.exception.task.TaskNotFoundException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.task.circuit.UserServiceClientWrapper;
import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.dto.comment.CommentScrollResponse;
import com.example.colaba.task.dto.comment.CreateCommentRequest;
import com.example.colaba.task.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.entity.CommentJpa;
import com.example.colaba.task.mapper.CommentMapper;
import com.example.colaba.task.repository.CommentRepository;
import com.example.colaba.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserServiceClientWrapper userServiceClient;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        boolean userExists = userServiceClient.userExists(request.userId());
        if (!userExists) {
            throw new UserNotFoundException(request.userId());
        }

        if (!taskRepository.existsById(request.taskId())) {
            throw new TaskNotFoundException(request.taskId());
        }

        CommentJpa comment = CommentJpa.builder()
                .taskId(request.taskId())
                .userId(request.userId())
                .content(request.content())
                .build();

        CommentJpa saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    public CommentResponse getCommentById(Long id) {
        CommentJpa comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        return commentMapper.toResponse(comment);
    }

    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        Page<CommentJpa> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return commentMapper.toResponsePage(comments);
    }

    public CommentScrollResponse getCommentsByTaskScroll(Long taskId, String cursor, int limit) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        OffsetDateTime cursorTime = (cursor == null || cursor.isBlank())
                ? OffsetDateTime.now()
                : OffsetDateTime.parse(cursor);
        Pageable pageable = PageRequest.of(
                0, limit, Sort.by("createdAt").descending());
        Slice<CommentJpa> slice = commentRepository
                .findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(taskId, cursorTime, pageable);
        List<CommentResponse> responses = commentMapper
                .toResponseList(slice.getContent());
        String nextCursor = slice.isEmpty()
                ? null
                : slice.getContent()
                .getLast().getCreatedAt().toString();
        return new CommentScrollResponse(
                responses, nextCursor, slice.hasNext());
    }

    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request) {
        CommentJpa comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        boolean hasChanges = false;
        if (request.content() != null
                && !request.content().isBlank()
                && !request.content().equals(comment.getContent())) {
            comment.setContent(request.content());
            hasChanges = true;
        }

        CommentJpa saved = hasChanges ? commentRepository.save(comment) : comment;
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