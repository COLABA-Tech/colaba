package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.task.application.ports.CommentRepositoryPort;
import com.example.colaba.task.domain.entity.Comment;
import com.example.colaba.task.infrastructure.persistence.entity.CommentJpa;
import com.example.colaba.task.infrastructure.persistence.mapper.CommentMapper;
import com.example.colaba.task.infrastructure.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepositoryPort {

    private final CommentRepository jpaRepo;
    private final CommentMapper mapper;

    @Override
    public Comment save(Comment comment) {
        CommentJpa jpa = mapper.toJpa(comment);
        CommentJpa saved = jpaRepo.save(jpa);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepo.deleteById(id);
    }

    @Override
    public Page<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, Pageable pageable) {
        return jpaRepo.findByTaskIdOrderByCreatedAtDesc(taskId, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Slice<Comment> findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long taskId, OffsetDateTime cursorTime, Pageable pageable) {
        return jpaRepo.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(taskId, cursorTime, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        jpaRepo.deleteByTaskId(taskId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRepo.deleteByUserId(userId);
    }
}