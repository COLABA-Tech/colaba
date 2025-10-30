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

    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        Project project = projectService.getProjectEntityById(projectId);
        return taskMapper.toTaskResponsePage(taskRepository.findByProject(project, pageable));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectService.getProjectEntityById(request.projectId());
        TaskStatus status = (request.status() != null) ? request.status() : TaskStatus.getDefault();
        TaskPriority priority = (request.priority() != null) ? request.priority() : null;
        User reporter = userService.getUserEntityById(request.reporterId());

        User assignee = null;
        if (request.assigneeId() != null) {
            assignee = userService.getUserEntityById(request.assigneeId());
        }

        Task task = new Task(
                request.title(),
                request.description(),
                status,
                priority,
                project,
                assignee,
                reporter,
                request.dueDate()
        );

        Task savedTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            TaskStatus status = request.status();
            task.setStatus(status);
        }
        if (request.priority() != null) {
            TaskPriority priority = request.priority();
            task.setPriority(priority);
        }
        if (request.assigneeId() != null) {
            User assignee = userService.getUserEntityById(request.assigneeId());
            task.setAssignee(assignee);
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }

        Task updatedTask = taskRepository.save(task);
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
}
