package com.example.colaba.task.application.ports;

import java.util.List;

public interface TaskTagRepositoryPort {
    List<Long> findTagIdsByTaskId(Long taskId);

    boolean existsByTaskIdAndTagId(Long taskId, Long tagId);

    void saveTaskTag(Long taskId, Long tagId);

    void deleteByTaskIdAndTagId(Long taskId, Long tagId);

    void deleteByTaskId(Long taskId);

    void deleteByTagId(Long tagId);
}
