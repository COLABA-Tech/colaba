package com.example.colaba.task.repository;

import com.example.colaba.shared.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable);

    Slice<Comment> findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long taskId, OffsetDateTime createdBefore, Pageable pageable);

    long countByTaskId(Long taskId);

    List<Comment> findAllByTaskId(Long taskId);
}