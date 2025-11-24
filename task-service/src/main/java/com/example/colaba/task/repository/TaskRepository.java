package com.example.colaba.task.repository;

import com.example.colaba.project.entity.Project;
import com.example.colaba.user.entity.User;
import com.example.colaba.task.entity.task.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByProject(Project project, Pageable pageable);

    Page<Task> findByAssignee(User assignee, Pageable pageable);
}