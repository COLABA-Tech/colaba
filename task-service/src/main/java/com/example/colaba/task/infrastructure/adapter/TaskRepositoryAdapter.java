package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.task.application.ports.TaskRepositoryPort;
import com.example.colaba.task.domain.entity.Task;
import com.example.colaba.task.infrastructure.persistence.entity.TaskJpa;
import com.example.colaba.task.infrastructure.persistence.mapper.TaskMapperJpa;
import com.example.colaba.task.infrastructure.persistence.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskRepository jpaRepo;
    private final TaskMapperJpa mapper;

    @Override
    public PagedResult<Task> findAll(PaginationRequest pageable) {
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(pageable.sortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                pageable.sortBy()
        );
        Pageable springPageable = PageRequest.of(pageable.page(), pageable.size(), sort);
        Page<TaskJpa> springPage = jpaRepo.findAll(springPageable);
        List<Task> domainList = springPage.map(mapper::toDomain).getContent();
        return new PagedResult<>(
                domainList,
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                pageable.page(),
                pageable.size()
        );
    }

    @Override
    public Optional<Task> findById(Long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Task save(Task task) {
        TaskJpa jpa = mapper.toJpa(task);
        TaskJpa saved = jpaRepo.save(jpa);
        return mapper.toDomain(saved);
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
    public PagedResult<Task> findByProjectId(Long projectId, PaginationRequest pageable) {
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(pageable.sortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                pageable.sortBy()
        );
        Pageable springPageable = PageRequest.of(pageable.page(), pageable.size(), sort);
        Page<TaskJpa> springPage = jpaRepo.findByProjectId(projectId, springPageable);
        List<Task> domainList = springPage.map(mapper::toDomain).getContent();
        return new PagedResult<>(
                domainList,
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                pageable.page(),
                pageable.size()
        );
    }

    @Override
    public PagedResult<Task> findByAssigneeId(Long assigneeId, PaginationRequest pageable) {
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(pageable.sortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                pageable.sortBy()
        );
        Pageable springPageable = PageRequest.of(pageable.page(), pageable.size(), sort);
        Page<TaskJpa> springPage = jpaRepo.findByAssigneeId(assigneeId, springPageable);
        List<Task> domainList = springPage.map(mapper::toDomain).getContent();
        return new PagedResult<>(
                domainList,
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                pageable.page(),
                pageable.size()
        );
    }

    @Override
    public List<Task> findAllByProjectId(Long projectId) {
        return jpaRepo.findAllByProjectId(projectId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void setReporterIdToNull(Long userId) {
        jpaRepo.setReporterIdToNullByReporterId(userId);
    }

    @Override
    public void setAssigneeIdToNull(Long assigneeId) {
        jpaRepo.setAssigneeIdToNullByAssigneeId(assigneeId);
    }
}