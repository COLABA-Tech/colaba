package com.example.colaba.repository;

import com.example.colaba.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Пагинация по task с total (для header X-Total-Count)
    Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);

    // Infinite scroll: Slice без total, по createdAt before (стандарт для timeline)
    Slice<Comment> findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long taskId, OffsetDateTime createdBefore, Pageable pageable);

    // Total count для task (дублирует Page, но explicit ок)
    long countByTaskId(Long taskId);

    // Добавка: Все комментарии по task для bulk-операций (в транзакции)
    List<Comment> findAllByTaskId(Long taskId);
}