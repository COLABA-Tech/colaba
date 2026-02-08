package com.example.colaba.task.application.ports;

import com.example.colaba.task.domain.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TaskRepositoryPort {
    Page<Task> findAll(Pageable pageable);

    Optional<Task> findById(Long id);

    Task save(Task task);

    boolean existsById(Long id);

    void deleteById(Long id);

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    List<Task> findAllByProjectId(Long projectId);

    void setReporterIdToNull(Long userId);

    void setAssigneeIdToNull(Long assigneeId);
}