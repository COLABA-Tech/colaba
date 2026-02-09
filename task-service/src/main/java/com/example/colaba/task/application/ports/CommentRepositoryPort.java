package com.example.colaba.task.application.ports;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.task.domain.entity.Comment;

import java.util.Optional;

public interface CommentRepositoryPort {
    Comment save(Comment comment);

    Optional<Comment> findById(Long id);

    boolean existsById(Long id);

    void deleteById(Long id);

    PagedResult<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, PaginationRequest pageable);

    void deleteByTaskId(Long taskId);

    void deleteByUserId(Long userId);
}
