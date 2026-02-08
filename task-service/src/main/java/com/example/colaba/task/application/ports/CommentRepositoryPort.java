package com.example.colaba.task.application.ports;

import com.example.colaba.task.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CommentRepositoryPort {
    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);

    Slice<Comment> findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long taskId, OffsetDateTime cursorTime, Pageable pageable);

    void deleteByTaskId(Long taskId);

    void deleteByUserId(Long userId);
}
