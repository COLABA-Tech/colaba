package com.example.colaba.task.repository;

import com.example.colaba.task.entity.CommentJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface CommentRepository extends JpaRepository<CommentJpa, Long> {
    Page<CommentJpa> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);

    Slice<CommentJpa> findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long taskId, OffsetDateTime cursorTime, Pageable pageable);

    @Modifying
    @Query("DELETE FROM CommentJpa c WHERE c.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("DELETE FROM CommentJpa c WHERE c.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}