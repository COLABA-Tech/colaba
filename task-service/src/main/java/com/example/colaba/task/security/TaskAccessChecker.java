package com.example.colaba.task.security;

import com.example.colaba.shared.common.exception.task.TaskNotFoundException;
import com.example.colaba.shared.webmvc.circuit.ProjectServiceClientWrapper;
import com.example.colaba.task.entity.task.TaskJpa;
import com.example.colaba.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAccessChecker {

    private final TaskRepository taskRepository;
    private final ProjectServiceClientWrapper projectServiceClient;

    /**
     * Требует, чтобы пользователь был OWNER'ом проекта задачи.
     * VIEWER и EDITOR не имеют доступа.
     */
    public void requireOwner(Long taskId, Long userId) {
        TaskJpa task = getTaskOrThrow(taskId);
        boolean isOwner = projectServiceClient.isOwner(task.getProjectId(), userId);

        if (!isOwner) {
            throw new AccessDeniedException("Only the project OWNER can perform this action");
        }
    }

    /**
     * Требует, чтобы пользователь был хотя бы EDITOR'ом проекта задачи.
     * VIEWER не имеет доступа.
     */
    public void requireAtLeastEditor(Long taskId, Long userId) {
        TaskJpa task = getTaskOrThrow(taskId);
        boolean isAtLeastEditor = projectServiceClient.isAtLeastEditor(task.getProjectId(), userId);

        if (!isAtLeastEditor) {
            String role = projectServiceClient.getUserProjectRole(task.getProjectId(), userId);
            throw new AccessDeniedException(
                    String.format("Required project role: at least EDITOR. Current role: %s",
                            role != null ? role : "NONE"));
        }
    }

    /**
     * Требует, чтобы пользователь имел любую роль в проекте задачи (VIEWER, EDITOR или OWNER).
     */
    public void requireAnyRole(Long taskId, Long userId) {
        TaskJpa task = getTaskOrThrow(taskId);
        boolean hasAnyRole = projectServiceClient.hasAnyRole(task.getProjectId(), userId);

        if (!hasAnyRole) {
            throw new AccessDeniedException("You must be a member of this project to perform this action");
        }
    }

    /**
     * Вспомогательный метод для получения задачи
     */
    private TaskJpa getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
}