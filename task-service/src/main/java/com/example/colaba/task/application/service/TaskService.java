package com.example.colaba.task.application.service;

import com.example.colaba.shared.common.application.dto.tag.TagResponse;
import com.example.colaba.shared.common.domain.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.domain.exception.tag.TagNotFoundException;
import com.example.colaba.shared.common.domain.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.domain.exception.user.UserNotFoundException;
import com.example.colaba.task.application.dto.task.CreateTaskRequest;
import com.example.colaba.task.application.dto.task.TaskResponse;
import com.example.colaba.task.application.dto.task.UpdateTaskRequest;
import com.example.colaba.task.application.ports.*;
import com.example.colaba.task.domain.entity.Task;
import com.example.colaba.task.domain.enums.TaskPriority;
import com.example.colaba.task.infrastructure.persistence.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepositoryPort taskRepository;
    private final TaskTagRepositoryPort taskTagRepository;
    private final CommentRepositoryPort commentRepository;
    private final ProjectServicePort projectServiceClient;
    private final UserServicePort userServiceClient;
    private final TaskMapper taskMapper;

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskMapper.toTaskResponsePage(taskRepository.findAll(pageable));
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toTaskResponse(task);
    }

    public Task getTaskEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        boolean projectExists = projectServiceClient.projectExists(projectId);
        if (!projectExists) {
            throw new ProjectNotFoundException(projectId);
        }
        return taskMapper.toTaskResponsePage(taskRepository.findByProjectId(projectId, pageable));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long reporterId) {
        boolean projectExists = projectServiceClient.projectExists(request.projectId());
        if (!projectExists) {
            throw new ProjectNotFoundException(request.projectId());
        }

        if (request.assigneeId() != null) {
            boolean assigneeExists = userServiceClient.userExists(request.assigneeId());
            if (!assigneeExists) {
                throw new UserNotFoundException(request.assigneeId());
            }
        }

        boolean reporterExists = userServiceClient.userExists(reporterId);
        if (!reporterExists) {
            throw new UserNotFoundException(reporterId);
        }

        TaskPriority priority = (request.priority() != null) ? request.priority() : null;

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(priority)
                .projectId(request.projectId())
                .assigneeId(request.assigneeId())
                .reporterId(reporterId)
                .dueDate(request.dueDate())
                .build();

        Task savedTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        boolean hasChanges = false;

        if (request.title() != null && !request.title().equals(task.getTitle())) {
            task.setTitle(request.title());
            hasChanges = true;
        }
        if (request.description() != null && !request.description().equals(task.getDescription())) {
            task.setDescription(request.description());
            hasChanges = true;
        }
        if (request.status() != null && !request.status().equals(task.getStatus())) {
            task.setStatus(request.status());
            hasChanges = true;
        }
        if (request.priority() != null && !request.priority().equals(task.getPriority())) {
            task.setPriority(request.priority());
            hasChanges = true;
        }
        if (request.assigneeId() != null && !request.assigneeId().equals(task.getAssigneeId())) {
            boolean assigneeExists = userServiceClient.userExists(request.assigneeId());
            if (!assigneeExists) {
                throw new UserNotFoundException(request.assigneeId());
            }
            task.setAssigneeId(request.assigneeId());
            hasChanges = true;
        }
        if (request.dueDate() != null && !request.dueDate().equals(task.getDueDate())) {
            task.setDueDate(request.dueDate());
            hasChanges = true;
        }

        Task updatedTask = hasChanges ? taskRepository.save(task) : task;
        return taskMapper.toTaskResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskTagRepository.deleteByTaskId(id);
        commentRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    public Page<TaskResponse> getTasksByAssignee(Long userId, Pageable pageable) {
        boolean userExists = userServiceClient.userExists(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }
        return taskMapper.toTaskResponsePage(taskRepository.findByAssigneeId(userId, pageable));
    }

    public List<TagResponse> getTagsByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        List<Long> tagIds = taskTagRepository.findTagIdsByTaskId(taskId);
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return projectServiceClient.getTagsByIds(tagIds);
    }

    @Transactional
    public void assignTagToTask(Long taskId, Long tagId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        TagResponse tag = projectServiceClient.getTagById(tagId);
        if (tag == null) {
            throw new TagNotFoundException(tagId);
        }
        if (!tag.projectId().equals(task.getProjectId())) {
            throw new IllegalArgumentException(
                    String.format("Tag %d does not belong to project %d",
                            tagId, task.getProjectId())
            );
        }
        if (taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)) {
            return;
        }
        taskTagRepository.saveTaskTag(taskId, tagId);
    }

    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        taskTagRepository.deleteByTaskIdAndTagId(taskId, tagId);
    }

    @Transactional
    public void deleteTasksByProject(Long projectId) {
        List<Task> tasks = taskRepository.findAllByProjectId(projectId);
        tasks.forEach(task -> deleteTask(task.getId()));
    }

    @Transactional
    public void handleUserDeletion(Long userId) {
        taskRepository.setReporterIdToNull(userId);
        taskRepository.setAssigneeIdToNull(userId);
        commentRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteTaskTagsByTagId(Long tagId) {
        taskTagRepository.deleteByTagId(tagId);
    }

    @Transactional
    public void handleProjectDeletion(Long projectId) {
        deleteTasksByProject(projectId);
        log.info("Deleted all tasks and related entities for projectId={}", projectId);
    }

    @Transactional
    public void handleTagDeletion(Long tagId) {
        deleteTaskTagsByTagId(tagId);
        log.info("Deleted all task-tag links for deleted tagId={}", tagId);
    }
}
