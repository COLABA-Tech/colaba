package com.example.colaba.task.service;

import com.example.colaba.shared.dto.comment.CommentResponse;
import com.example.colaba.shared.dto.comment.CommentScrollResponse;
import com.example.colaba.shared.dto.comment.CreateCommentRequest;
import com.example.colaba.shared.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.entity.Comment;
import com.example.colaba.user.entity.User;
import com.example.colaba.task.entity.task.Task;
import com.example.colaba.shared.exception.comment.CommentNotFoundException;
import com.example.colaba.shared.exception.comment.TaskNotFoundException;
import com.example.colaba.shared.exception.comment.UserNotFoundException;
import com.example.colaba.task.mapper.CommentMapper;
import com.example.colaba.task.repository.CommentRepository;
import com.example.colaba.task.repository.TaskRepository;
import com.example.colaba.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(CreateCommentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new TaskNotFoundException(request.taskId()));

        Comment comment = Comment.builder()
                .task(task)
                .user(user)
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

    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return commentMapper.toResponsePage(comments);
    }

    public CommentScrollResponse getCommentsByTaskScroll(Long taskId, String cursor, int limit) {
        OffsetDateTime cursorTime = (cursor == null || cursor.isBlank())
                ? OffsetDateTime.now()
                : OffsetDateTime.parse(cursor);

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());

        Slice<Comment> slice = commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(taskId, cursorTime, pageable);

        List<CommentResponse> responses = commentMapper.toResponseList(slice.getContent());

        String nextCursor = slice.isEmpty() ? null : slice.getContent().get(slice.getContent().size() - 1).getCreatedAt().toString();

        CommentScrollResponse resp = new CommentScrollResponse(responses, nextCursor, slice.hasNext());

        return resp;
    }

    @Transactional
    public CommentResponse updateComment(Long id, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        boolean hasChanges = false;
        if (request.content() != null && !request.content().isBlank() && !request.content().equals(comment.getContent())) {
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

    @Transactional
    public long countCommentsByTask(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return commentRepository.countByTaskId(taskId);
    }

    @Transactional
    public void bulkUpdateContentForTask(Long taskId, String prefix) {
        List<Comment> comments = commentRepository.findAllByTaskId(taskId);
        comments.forEach(c -> c.setContent(prefix + c.getContent()));
        commentRepository.saveAll(comments);
    }
}