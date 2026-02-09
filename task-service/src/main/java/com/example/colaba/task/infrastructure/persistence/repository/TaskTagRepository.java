package com.example.colaba.task.infrastructure.persistence.repository;

import com.example.colaba.task.domain.entity.TaskTagId;
import com.example.colaba.task.infrastructure.persistence.entity.TaskTagJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTagJpa, TaskTagId> {
    @Query("SELECT tt.tagId FROM TaskTagJpa tt WHERE tt.taskId = :taskId")
    List<Long> findTagIdsByTaskId(@Param("taskId") Long taskId);

    boolean existsByTaskIdAndTagId(Long taskId, Long tagId);

    void deleteByTaskIdAndTagId(Long taskId, Long tagId);

    void deleteByTaskId(Long taskId);

    void deleteByTagId(Long tagId);
}
