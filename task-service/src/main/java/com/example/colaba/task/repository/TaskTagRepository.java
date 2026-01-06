package com.example.colaba.task.repository;

import com.example.colaba.shared.entity.tasktag.TaskTag;
import com.example.colaba.shared.entity.tasktag.TaskTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, TaskTagId> {
    @Query("SELECT tt.tagId FROM TaskTag tt WHERE tt.taskId = :taskId")
    List<Long> findTagIdsByTaskId(@Param("taskId") Long taskId);

    boolean existsByTaskIdAndTagId(Long taskId, Long tagId);

    void deleteByTaskIdAndTagId(Long taskId, Long tagId);

    void deleteByTaskId(Long taskId);
}
