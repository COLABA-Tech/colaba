package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.task.application.ports.CommentRepositoryPort;
import com.example.colaba.task.domain.entity.Comment;
import com.example.colaba.task.infrastructure.persistence.entity.CommentJpa;
import com.example.colaba.task.infrastructure.persistence.mapper.CommentMapperJpa;
import com.example.colaba.task.infrastructure.persistence.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentRepositoryAdapter implements CommentRepositoryPort {

    private final CommentRepository jpaRepo;
    private final CommentMapperJpa mapper;

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
    public PagedResult<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId, PaginationRequest pageable) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable springPageable = PageRequest.of(pageable.page(), pageable.size(), sort);
        Page<CommentJpa> springPage = jpaRepo.findByTaskIdOrderByCreatedAtDesc(taskId, springPageable);
        List<Comment> domainList = springPage.map(mapper::toDomain).getContent();
        return new PagedResult<>(
                domainList,
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                pageable.page(),
                pageable.size()
        );
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