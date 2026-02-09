package com.example.colaba.task.infrastructure.service;

import com.example.colaba.task.application.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceFacade {
    private final TaskService taskService;

    @Transactional
    public void deleteTasksByProject(Long projectId) {
        taskService.deleteTasksByProject(projectId);
    }

    @Transactional
    public void handleUserDeletion(Long userId) {
        taskService.handleUserDeletion(userId);
    }

    @Transactional
    public void handleProjectDeletion(Long projectId) {
        taskService.handleProjectDeletion(projectId);
    }

    @Transactional
    public void handleTagDeletion(Long tagId) {
        taskService.handleTagDeletion(tagId);
    }

    @Transactional
    public void deleteTaskTagsByTagId(Long tagId) {
        taskService.deleteTaskTagsByTagId(tagId);
    }
}
