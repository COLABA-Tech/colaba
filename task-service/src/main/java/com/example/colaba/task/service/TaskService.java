package com.example.colaba.task.service;

import com.example.colaba.shared.circuit.ProjectClientWrapper;
import com.example.colaba.shared.circuit.UserClientWrapper;
import com.example.colaba.shared.dto.task.CreateTaskRequest;
import com.example.colaba.shared.dto.task.TaskResponse;
import com.example.colaba.shared.dto.task.UpdateTaskRequest;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.User;
import com.example.colaba.shared.entity.UserJpa;
import com.example.colaba.shared.entity.task.Task;
import com.example.colaba.shared.entity.task.TaskPriority;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.exception.task.TaskNotFoundException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.shared.mapper.TaskMapper;
import com.example.colaba.shared.mapper.UserMapper;
import com.example.colaba.task.repository.TaskRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectClientWrapper projectClientWrapper;  // Через обёртку
    private final UserClientWrapper userClientWrapper;        // Через обёртку
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskMapper.toTaskResponsePage(taskRepository.findAll(pageable));
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toTaskResponse(task);
    }

    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        Project project;
        try {
            project = projectClientWrapper.getProjectEntityById(projectId);
        } catch (FeignException.NotFound e) {
            throw new ProjectNotFoundException(projectId);
        }
        return taskMapper.toTaskResponsePage(taskRepository.findByProject(project, pageable));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project;
        try {
            project = projectClientWrapper.getProjectEntityById(request.projectId());
        } catch (FeignException.NotFound e) {
            throw new ProjectNotFoundException(request.projectId());
        }

        UserJpa assignee = null;
        if (request.assigneeId() != null) {
            try {
                User user = userClientWrapper.getUserEntityById(request.assigneeId());
                assignee = userMapper.toUserJpa(user);
            } catch (FeignException.NotFound e) {
                throw new UserNotFoundException(request.assigneeId());
            }
        }

        UserJpa reporter;
        try {
            User reporterUser = userClientWrapper.getUserEntityById(request.reporterId());
            reporter = userMapper.toUserJpa(reporterUser);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException(request.reporterId());
        }

        TaskPriority priority = request.priority() != null ? request.priority() : null;

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(priority)
                .project(project)
                .assignee(assignee)
                .reporter(reporter)
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
        if (request.assigneeId() != null &&
                (task.getAssignee() == null || !request.assigneeId().equals(task.getAssignee().getId()))) {
            try {
                User user = userClientWrapper.getUserEntityById(request.assigneeId());
                task.setAssignee(userMapper.toUserJpa(user));
                hasChanges = true;
            } catch (FeignException.NotFound e) {
                throw new UserNotFoundException(request.assigneeId());
            }
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
        taskRepository.deleteById(id);
    }

    public Page<TaskResponse> getTasksByAssignee(Long userId, Pageable pageable) {
        User user;
        try {
            user = userClientWrapper.getUserEntityById(userId);
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException(userId);
        }
        UserJpa assignee = userMapper.toUserJpa(user);
        return taskMapper.toTaskResponsePage(taskRepository.findByAssignee(assignee, pageable));
    }
}