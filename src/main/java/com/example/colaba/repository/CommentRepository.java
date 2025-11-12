package com.example.colaba.repository;

import com.example.colaba.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;  // Добавил для infinite scroll без total
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;  // Если нужно кастом, но пока derivation
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;  // Добавил для findByIdAndTaskId

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Пагинация по task с total (для header X-Total-Count)
    Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);

    // Infinite scroll: Slice без total, по createdAt before (стандарт для timeline)
    Slice<Comment> findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long taskId, OffsetDateTime createdBefore, Pageable pageable);

    // Total count для task (дублирует Page, но explicit ок)
    long countByTaskId(Long taskId);

    // Добавка: Проверка comment по id и task (для update/delete ownership)
    Optional<Comment> findByIdAndTaskId(Long id, Long taskId);

    // Добавка: Все комментарии по task для bulk-операций (в транзакции)
    List<Comment> findAllByTaskId(Long taskId);
}