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
import com.example.colaba.mapper.UserMapper;
import com.example.colaba.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskMapper.toTaskResponsePage(taskRepository.findAll(pageable));
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByProject(Long projectId, Pageable pageable) {
        Project project = projectService.getProjectEntityById(projectId);
        return taskMapper.toTaskResponsePage(taskRepository.findByProject(project, pageable));
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        Project project = projectService.getProjectEntityById(request.getProjectId());
        TaskStatus status = (request.getStatus() != null) ? request.getStatus() : TaskStatus.getDefault();
        TaskPriority priority = (request.getPriority() != null) ? request.getPriority() : null;
        User reporter = userService.getUserEntityById(request.getReporterId());

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userService.getUserEntityById(request.getAssigneeId());
        }

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                status,
                priority,
                project,
                assignee,
                reporter,
                request.getDueDate()
        );

        Task savedTask = taskRepository.save(task);
        return taskMapper.toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            TaskStatus status = request.getStatus();
            task.setStatus(status);
        }
        if (request.getPriority() != null) {
            TaskPriority priority = request.getPriority();
            task.setPriority(priority);
        }
        if (request.getAssigneeId() != null) {
            User assignee = userService.getUserEntityById(request.getAssigneeId());
            task.setAssignee(assignee);
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
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
