package com.example.colaba.service;

import com.example.colaba.dto.comment.CommentResponse;
import com.example.colaba.dto.comment.CommentScrollResponse;
import com.example.colaba.dto.comment.CreateCommentRequest;
import com.example.colaba.dto.comment.UpdateCommentRequest;
import com.example.colaba.entity.Comment;
import com.example.colaba.entity.User;
import com.example.colaba.entity.task.Task;
import com.example.colaba.exception.comment.CommentNotFoundException;
import com.example.colaba.exception.comment.TaskNotFoundException;
import com.example.colaba.exception.comment.UserNotFoundException;
import com.example.colaba.mapper.CommentMapper;
import com.example.colaba.repository.CommentRepository;
import com.example.colaba.repository.TaskRepository;
import com.example.colaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok: constructor injection
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;  // Новый dep

    @Transactional  // Write: override readOnly
    public CommentResponse createComment(CreateCommentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new TaskNotFoundException(request.taskId()));

        // Builder: fluent, легко читать/расширять
        Comment comment = Comment.builder()
                .task(task)
                .user(user)
                .content(request.content())
                .build();  // createdAt auto от @CreationTimestamp — не нужно set

        Comment saved = commentRepository.save(comment);
        return commentMapper.toResponse(saved);
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        return commentMapper.toResponse(comment);
    }

    public Page<CommentResponse> getCommentsByTask(Long taskId, Pageable pageable) {
        // Enforce size <=50 в контроллере
        Page<Comment> comments = commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable);
        return commentMapper.toResponsePage(comments);
    }

    public CommentScrollResponse getCommentsByTaskScroll(Long taskId, String cursor, int limit) {
        OffsetDateTime cursorTime = (cursor == null || cursor.isBlank())
                ? OffsetDateTime.now()  // Начало: самые новые
                : OffsetDateTime.parse(cursor);  // Parse с handle в контроллере

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());  // Desc: новые сверху

        // Прямой Slice из repo — без wrap!
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

    @Transactional  // Write
    public void deleteComment(Long id) {
        // Ownership: аналогично, pass taskId if needed
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException(id);
        }
        commentRepository.deleteById(id);
    }

    @Transactional
    public long countCommentsByTask(Long taskId) {
        // Добавили проверку: throw если task не существует
        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return commentRepository.countByTaskId(taskId);
    }

    // Добавка: Сложный запрос с транзакцией (bulk update, e.g., prefix content для task)
    @Transactional
    // Обоснование: fetch + multiple updates — atomic, consistency (все комментарии обновлены или rollback)
    public void bulkUpdateContentForTask(Long taskId, String prefix) {
        List<Comment> comments = commentRepository.findAllByTaskId(taskId);
        comments.forEach(c -> c.setContent(prefix + c.getContent()));
        commentRepository.saveAll(comments);  // Batch save
    }
}