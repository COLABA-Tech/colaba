package com.example.colaba.service;

import com.example.colaba.dto.task.CreateTaskRequest;
import com.example.colaba.dto.task.TaskResponse;
import com.example.colaba.dto.task.UpdateTaskRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.User;
import com.example.colaba.entity.task.Task;
import com.example.colaba.entity.task.TaskPriority;
import com.example.colaba.entity.task.TaskStatus;
import com.example.colaba.exception.task.TaskNotFoundException;
import com.example.colaba.mapper.TaskMapper;
import com.example.colaba.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;
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
        Project project = projectService.getProjectEntityById(projectId);
        return taskMapper.toTaskResponsePage(taskRepository.findByProject(project, pageable));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectService.getProjectEntityById(request.projectId());
        TaskPriority priority = (request.priority() != null) ? request.priority() : null;
        User assignee = (request.assigneeId() != null) ? userService.getUserEntityById(request.assigneeId()) : null;
        User reporter = userService.getUserEntityById(request.reporterId());

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
            TaskStatus status = request.status();
            task.setStatus(status);
            hasChanges = true;
        }
        if (request.priority() != null && !request.priority().equals(task.getPriority())) {
            TaskPriority priority = request.priority();
            task.setPriority(priority);
            hasChanges = true;
        }
        if (request.assigneeId() != null && !request.assigneeId().equals(task.getAssignee().getId())) {
            User assignee = userService.getUserEntityById(request.assigneeId());
            task.setAssignee(assignee);
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
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByAssignee(Long userId, Pageable pageable) {
        User assignee = userService.getUserEntityById(userId);
        return taskMapper.toTaskResponsePage(taskRepository.findByAssignee(assignee, pageable));
    }

    @Transactional
    public void saveTask(Task task) {
        taskRepository.save(task);
    }
}
