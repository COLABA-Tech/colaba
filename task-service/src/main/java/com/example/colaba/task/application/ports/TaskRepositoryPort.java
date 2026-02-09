package com.example.colaba.task.application.ports;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.shared.common.application.dto.common.PaginationRequest;
import com.example.colaba.task.domain.entity.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    PagedResult<Task> findAll(PaginationRequest pageable);

    Optional<Task> findById(Long id);

    Task save(Task task);

    boolean existsById(Long id);

    void deleteById(Long id);

    PagedResult<Task> findByProjectId(Long projectId, PaginationRequest pageable);

    PagedResult<Task> findByAssigneeId(Long assigneeId, PaginationRequest pageable);

    List<Task> findAllByProjectId(Long projectId);

    void setReporterIdToNull(Long userId);

    void setAssigneeIdToNull(Long assigneeId);
}