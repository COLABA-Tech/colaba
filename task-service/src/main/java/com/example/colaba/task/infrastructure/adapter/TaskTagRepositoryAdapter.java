package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.task.application.ports.TaskTagRepositoryPort;
import com.example.colaba.task.infrastructure.persistence.entity.TaskTagJpa;
import com.example.colaba.task.infrastructure.persistence.repository.TaskTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskTagRepositoryAdapter implements TaskTagRepositoryPort {
    private final TaskTagRepository repo;

    @Override
    public List<Long> findTagIdsByTaskId(Long taskId) {
        return repo.findTagIdsByTaskId(taskId);
    }

    @Override
    public boolean existsByTaskIdAndTagId(Long taskId, Long tagId) {
        return repo.existsByTaskIdAndTagId(taskId, tagId);
    }

    @Override
    public void saveTaskTag(Long taskId, Long tagId) {
        repo.save(new TaskTagJpa(taskId, tagId));
    }

    @Override
    public void deleteByTaskIdAndTagId(Long taskId, Long tagId) {
        repo.deleteByTaskIdAndTagId(taskId, tagId);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        repo.deleteByTaskId(taskId);
    }

    @Override
    public void deleteByTagId(Long tagId) {
        repo.deleteByTagId(tagId);
    }
}
