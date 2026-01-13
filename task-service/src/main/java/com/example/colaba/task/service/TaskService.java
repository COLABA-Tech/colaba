package com.example.colaba.task.service;

import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.tag.TagNotFoundException;
import com.example.colaba.shared.common.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.task.circuit.ProjectServiceClientWrapper;
import com.example.colaba.task.circuit.UserServiceClientWrapper;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.entity.task.TaskJpa;
import com.example.colaba.task.entity.task.TaskPriority;
import com.example.colaba.task.entity.tasktag.TaskTagJpa;
import com.example.colaba.task.mapper.TaskMapper;
import com.example.colaba.task.repository.CommentRepository;
import com.example.colaba.task.repository.TaskRepository;
import com.example.colaba.task.repository.TaskTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskTagRepository taskTagRepository;
    private final CommentRepository commentRepository;
    private final ProjectServiceClientWrapper projectServiceClient;
    private final UserServiceClientWrapper userServiceClient;
    private final TaskMapper taskMapper;

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskMapper.toTaskResponsePage(taskRepository.findAll(pageable));
    }

    public TaskResponse getTaskById(Long id) {
        TaskJpa task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toTaskResponse(task);
    }

    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        boolean projectExists = projectServiceClient.projectExists(projectId);
        if (!projectExists) {
            throw new ProjectNotFoundException(projectId);
        }
        return taskMapper.toTaskResponsePage(taskRepository.findByProjectId(projectId, pageable));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
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

        boolean reporterExists = userServiceClient.userExists(request.reporterId());
        if (!reporterExists) {
            throw new UserNotFoundException(request.reporterId());
        }

        TaskPriority priority = (request.priority() != null) ? request.priority() : null;

        TaskJpa task = TaskJpa.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(priority)
                .projectId(request.projectId())
                .assigneeId(request.assigneeId())
                .reporterId(request.reporterId())
                .dueDate(request.dueDate())
                .build();

        TaskJpa savedTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        TaskJpa task = taskRepository.findById(id)
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

        TaskJpa updatedTask = hasChanges ? taskRepository.save(task) : task;
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
        TaskJpa task = taskRepository.findById(taskId)
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
        TaskTagJpa taskTag = TaskTagJpa.builder()
                .taskId(taskId)
                .tagId(tagId)
                .build();
        taskTagRepository.save(taskTag);
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
        List<TaskJpa> tasks = taskRepository.findAllByProjectId(projectId);
        tasks.forEach(task -> deleteTask(task.getId()));
    }

    @Transactional
    public void handleUserDeletion(Long userId) {
        taskRepository.setReporterIdToNullByReporterId(userId);
        taskRepository.setAssigneeIdToNullByAssigneeId(userId);
        commentRepository.deleteByUserId(userId);
    }

    @Transactional
    public void deleteTaskTagsByTagId(Long tagId) {
        taskTagRepository.deleteByTagId(tagId);
    }
}
