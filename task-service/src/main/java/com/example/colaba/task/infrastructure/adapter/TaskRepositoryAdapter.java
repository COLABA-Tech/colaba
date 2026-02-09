package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.task.application.ports.TaskRepositoryPort;
import com.example.colaba.task.domain.entity.Task;
import com.example.colaba.task.infrastructure.persistence.entity.TaskJpa;
import com.example.colaba.task.infrastructure.persistence.mapper.TaskMapper;
import com.example.colaba.task.infrastructure.persistence.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskRepositoryAdapter implements TaskRepositoryPort {

    private final TaskRepository jpaRepo;
    private final TaskMapper mapper;

    @Override
    public Page<Task> findAll(Pageable pageable) {
        return jpaRepo.findAll(pageable).map(mapper::toDomain);
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
    public Page<Task> findByProjectId(Long projectId, Pageable pageable) {
        return jpaRepo.findByProjectId(projectId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable) {
        return jpaRepo.findByAssigneeId(assigneeId, pageable).map(mapper::toDomain);
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