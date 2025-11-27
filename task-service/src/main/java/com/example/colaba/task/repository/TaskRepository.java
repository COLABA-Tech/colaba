package com.example.colaba.task.repository;

import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import com.example.colaba.shared.entity.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProject(Project project, Pageable pageable);

    Page<Task> findByAssignee(UserJpa assignee, Pageable pageable);
}