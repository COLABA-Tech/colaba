package com.example.colaba.task.repository;

import com.example.colaba.task.entity.task.TaskJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskJpa, Long> {
    Page<TaskJpa> findByProjectId(Long projectId, Pageable pageable);

    Page<TaskJpa> findByAssigneeId(Long assigneeId, Pageable pageable);

    List<TaskJpa> findAllByProjectId(Long projectId);

    @Modifying
    @Query("UPDATE TaskJpa t SET t.reporterId = null WHERE t.reporterId = :userId")
    void setReporterIdToNullByReporterId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE TaskJpa t SET t.assigneeId = null WHERE t.assigneeId = :userId")
    void setAssigneeIdToNullByAssigneeId(@Param("userId") Long userId);
}