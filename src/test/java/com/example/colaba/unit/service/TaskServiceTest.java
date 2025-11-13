package com.example.colaba.unit.service;

import com.example.colaba.dto.task.CreateTaskRequest;
import com.example.colaba.dto.task.TaskResponse;
import com.example.colaba.dto.task.UpdateTaskRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.User;
import com.example.colaba.entity.task.Task;
import com.example.colaba.entity.task.TaskPriority;
import com.example.colaba.entity.task.TaskStatus;
import com.example.colaba.exception.task.TaskNotFoundException;
import com.example.colaba.exception.user.UserNotFoundException;
import com.example.colaba.mapper.TaskMapper;
import com.example.colaba.repository.TaskRepository;
import com.example.colaba.service.ProjectService;
import com.example.colaba.service.TaskService;
import com.example.colaba.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private CreateTaskRequest request;
    private UpdateTaskRequest updateRequest;
    private Task savedTask;
    private TaskResponse taskResponse;

    private final Long testId = 1L;
    private final String testTitle = "Test Task";
    private final String testDescription = "Test Description";
    private final TaskStatus testStatus = TaskStatus.TODO;
    private final TaskPriority testPriority = TaskPriority.LOW;
    private final Long testProjectId = 1L;
    private final Long testAssigneeId = 2L;
    private final Long testReporterId = 1L;
    private final LocalDate testDueDate = LocalDate.now();

    private Project testProject;
    private User testAssignee;
    private User testReporter;

    @BeforeEach
    void setUp() {
        testAssignee = User.builder().id(testAssigneeId).username("assignee").build();
        testReporter = User.builder().id(testReporterId).username("reporter").build();

        UserService userServiceProject = mock(UserService.class);
        when(userServiceProject.getUserEntityById(testReporterId)).thenReturn(testReporter);
        testProject = (new ProjectService(userServiceProject)).getProjectEntityById(testProjectId);

        savedTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .project(testProject)
                .assignee(testAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        taskResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                LocalDateTime.now(), LocalDateTime.now()
        );

        request = new CreateTaskRequest(
                testTitle, testDescription, testStatus, testPriority,
                testProjectId, testAssigneeId, testReporterId, testDueDate
        );

        updateRequest = new UpdateTaskRequest(
                "Updated Title", null, TaskStatus.IN_PROGRESS, null,
                testAssigneeId, testDueDate.plusDays(1)
        );
    }

    @Test
    void createTask_success() {
        // Given (arrange)
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testReporterId)).thenReturn(testReporter);
        when(userService.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);

        // When (act)
        TaskResponse result = taskService.createTask(request);

        // Then (assert)
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertEquals(testStatus.name(), result.status());
        assertEquals(testProjectId, result.projectId());
        assertEquals(testAssigneeId, result.assigneeId());
        assertEquals(testReporterId, result.reporterId());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(userService).getUserEntityById(testReporterId);
        verify(userService).getUserEntityById(testAssigneeId);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toTaskResponse(savedTask);
    }

    @Test
    void createTask_projectNotFound_throwsException() {
        // TODO
//        // Given
//        when(projectService.getProjectEntityById(testProjectId))
//                .thenThrow(new ProjectNotFoundException(testProjectId));
//
//        // When & Then
//        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
//                () -> taskService.createTask(request));
//        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
//        verify(userService, never()).getUserEntityById(anyLong());
//        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_reporterNotFound_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
        when(userService.getUserEntityById(testReporterId))
                .thenThrow(new UserNotFoundException(testReporterId));

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> taskService.createTask(request));
        assertEquals("User not found: ID " + testReporterId, exception.getMessage());
        verify(userService).getUserEntityById(testReporterId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_assigneeOptional_nullAssignee() {
        // Given
        CreateTaskRequest optionalRequest = new CreateTaskRequest(
                testTitle, testDescription, testStatus, testPriority,
                testProjectId, null, testReporterId, testDueDate
        );

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testReporterId)).thenReturn(testReporter);
        Task nullAssigneeTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .project(testProject)
                .assignee(null)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .build();
        when(taskRepository.save(any(Task.class))).thenReturn(nullAssigneeTask);
        when(taskMapper.toTaskResponse(nullAssigneeTask)).thenReturn(taskResponse); // Adjust response if needed

        // When
        TaskResponse result = taskService.createTask(optionalRequest);

        // Then
        assertEquals(testId, result.id());
        verify(userService).getUserEntityById(testReporterId);
        verifyNoMoreInteractions(userService);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_withNullPriority_setsPriorityToNull() {
        // Given
        CreateTaskRequest nullPriorityRequest = new CreateTaskRequest(
                testTitle, testDescription, testStatus, null, // null priority
                testProjectId, testAssigneeId, testReporterId, testDueDate
        );

        Task nullPriorityTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(null) // null priority
                .project(testProject)
                .assignee(testAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskResponse nullPriorityResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), null, // null priority in response
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testReporterId)).thenReturn(testReporter);
        when(userService.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
        when(taskRepository.save(any(Task.class))).thenReturn(nullPriorityTask);
        when(taskMapper.toTaskResponse(nullPriorityTask)).thenReturn(nullPriorityResponse);

        // When
        TaskResponse result = taskService.createTask(nullPriorityRequest);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertNull(result.priority()); // Verify null priority in response
        verify(projectService).getProjectEntityById(testProjectId);
        verify(userService).getUserEntityById(testReporterId);
        verify(userService).getUserEntityById(testAssigneeId);
        verify(taskRepository).save(argThat(task -> task.getPriority() == null)); // Verify saved task has null priority
        verify(taskMapper).toTaskResponse(nullPriorityTask);
    }

    @Test
    void createTask_withNullStatus_setsDefaultStatus() {
        // Given
        CreateTaskRequest nullStatusRequest = new CreateTaskRequest(
                testTitle, testDescription, null, testPriority, // null status
                testProjectId, testAssigneeId, testReporterId, testDueDate
        );

        Task defaultStatusTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(TaskStatus.getDefault())
                .priority(testPriority)
                .project(testProject)
                .assignee(testAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TaskResponse defaultStatusResponse = new TaskResponse(
                testId, testTitle, testDescription, TaskStatus.getDefault().name(), testPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testReporterId)).thenReturn(testReporter);
        when(userService.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
        when(taskRepository.save(any(Task.class))).thenReturn(defaultStatusTask);
        when(taskMapper.toTaskResponse(defaultStatusTask)).thenReturn(defaultStatusResponse);

        // When
        TaskResponse result = taskService.createTask(nullStatusRequest);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertEquals(TaskStatus.getDefault().name(), result.status()); // Verify default status in response
        verify(taskRepository).save(argThat(task -> TaskStatus.getDefault().equals(task.getStatus()))); // Verify saved task has default status
        verify(taskMapper).toTaskResponse(defaultStatusTask);
    }

    @Test
    void getTaskById_success() {
        // Given (arrange)
        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);

        // When (act)
        TaskResponse result = taskService.getTaskById(testId);

        // Then (assert)
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertEquals(testProjectId, result.projectId());
        verify(taskRepository).findById(testId);
        verify(taskMapper).toTaskResponse(savedTask);
    }

    @Test
    void getTaskById_notFound_throwsException() {
        // Given (arrange)
        when(taskRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskById(testId));
        assertEquals("Task not found: ID " + testId, exception.getMessage());
        verify(taskMapper, never()).toTaskResponse(any(Task.class));
    }

    @Test
    void getTaskEntityById_success() {
        // Given
        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));

        // When
        Task result = taskService.getTaskEntityById(testId);

        // Then
        assertEquals(testId, result.getId());
        assertEquals(testTitle, result.getTitle());
        assertEquals(testProjectId, result.getProject().getId());
        verify(taskRepository).findById(testId);
    }

    @Test
    void getTaskEntityById_notFound_throwsException() {
        // Given (arrange)
        when(taskRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.getTaskEntityById(testId));
        assertEquals("Task not found: ID " + testId, exception.getMessage());
    }

    @Test
    void getTasksByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> mockPage = new PageImpl<>(List.of(savedTask));
        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(taskRepository.findByProject(testProject, pageable)).thenReturn(mockPage);
        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<TaskResponse> result = taskService.getTasksByProject(testProjectId, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(taskRepository).findByProject(testProject, pageable);
        verify(taskMapper).toTaskResponsePage(mockPage);
    }

    @Test
    void getTasksByProject_projectNotFound_throwsException() {
        // TODO
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        when(projectService.getProjectEntityById(testProjectId))
//                .thenThrow(new ProjectNotFoundException(testProjectId));
//
//        // When & Then
//        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
//                () -> taskService.getTasksByProject(testProjectId, pageable));
//        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
//        verify(taskRepository, never()).findByProject(any(Project.class), any(Pageable.class));
    }

    @Test
    void getTasksByAssignee_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> mockPage = new PageImpl<>(List.of(savedTask));
        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));

        when(userService.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
        when(taskRepository.findByAssignee(testAssignee, pageable)).thenReturn(mockPage);
        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<TaskResponse> result = taskService.getTasksByAssignee(testAssigneeId, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(userService).getUserEntityById(testAssigneeId);
        verify(taskRepository).findByAssignee(testAssignee, pageable);
        verify(taskMapper).toTaskResponsePage(mockPage);
    }

    @Test
    void getTasksByAssignee_userNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserEntityById(testAssigneeId))
                .thenThrow(new UserNotFoundException(testAssigneeId));

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> taskService.getTasksByAssignee(testAssigneeId, pageable));
        assertEquals("User not found: ID " + testAssigneeId, exception.getMessage());
        verify(taskRepository, never()).findByAssignee(any(User.class), any(Pageable.class));
    }

    @Test
    void updateTask_success() {
        // Given
        Task updatedTask = Task.builder()
                .id(testId)
                .title(updateRequest.title())
                .status(updateRequest.status())
                .assignee(testAssignee)
                .dueDate(updateRequest.dueDate())
                .build();
        TaskResponse updatedResponse = new TaskResponse(
                testId, updateRequest.title(), testDescription, updateRequest.status().name(), testPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), updateRequest.dueDate(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, updateRequest);

        // Then
        assertEquals(testId, result.id());
        assertEquals(updateRequest.title(), result.title());
        assertEquals(updateRequest.status().name(), result.status());
        verify(taskRepository).findById(testId);
        verify(taskRepository).save(any(Task.class));
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void updateTask_noChanges_returnsUnchangedTask() {
        // Given
        UpdateTaskRequest noChangeRequest = new UpdateTaskRequest(
                testTitle, testDescription, testStatus, testPriority, null, testDueDate
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, noChangeRequest);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        verify(taskRepository).findById(testId);
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskMapper).toTaskResponse(savedTask);
    }

    @Test
    void updateTask_partialUpdate_ignoresNullFields() {
        // Given
        UpdateTaskRequest partialRequest = new UpdateTaskRequest(
                "Partial Title", null, null, null, null, null
        );
        Task partialUpdatedTask = Task.builder()
                .id(testId)
                .title("Partial Title")
                .build();
        TaskResponse partialResponse = new TaskResponse(
                testId, "Partial Title", testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(partialUpdatedTask);
        when(taskMapper.toTaskResponse(partialUpdatedTask)).thenReturn(partialResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, partialRequest);

        // Then
        assertEquals("Partial Title", result.title());
        assertEquals(testStatus.name(), result.status()); // unchanged
        verify(taskRepository).save(any(Task.class)); // save called due to title change
    }

    @Test
    void updateTask_notFound_throwsException() {
        // Given
        when(taskRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(testId, updateRequest));
        assertEquals("Task not found: ID " + testId, exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verify(userService, never()).getUserEntityById(anyLong());
        verify(taskMapper, never()).toTaskResponse(any(Task.class));
    }

    @Test
    void updateTask_changeTitle_updatesTitleAndSetsHasChanges() {
        // Given
        UpdateTaskRequest titleChangeRequest = new UpdateTaskRequest(
                "New Title", null, null, null, null, null
        );
        Task updatedTask = Task.builder()
                .id(testId)
                .title("New Title")
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .project(testProject)
                .assignee(testAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(savedTask.getCreatedAt())
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .build();
        TaskResponse updatedResponse = new TaskResponse(
                testId, "New Title", testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, titleChangeRequest);

        // Then
        assertEquals("New Title", result.title());
        verify(taskRepository).save(argThat(task -> "New Title".equals(task.getTitle()))); // Verify title changed
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void updateTask_changeDescription_updatesDescriptionAndSetsHasChanges() {
        // Given
        UpdateTaskRequest descriptionChangeRequest = new UpdateTaskRequest(
                null, "New Description", null, null, null, null
        );
        Task updatedTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description("New Description")
                .status(testStatus)
                .priority(testPriority)
                .project(testProject)
                .assignee(testAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(savedTask.getCreatedAt())
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .build();
        TaskResponse updatedResponse = new TaskResponse(
                testId, testTitle, "New Description", testStatus.name(), testPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, descriptionChangeRequest);

        // Then
        assertEquals("New Description", result.description());
        verify(taskRepository).save(argThat(task -> "New Description".equals(task.getDescription()))); // Verify description changed
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void updateTask_changePriority_updatesPriorityAndSetsHasChanges() {
        // Given
        TaskPriority newPriority = TaskPriority.MEDIUM;
        UpdateTaskRequest priorityChangeRequest = new UpdateTaskRequest(
                null, null, null, newPriority, null, null
        );
        Task updatedTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(newPriority)
                .project(testProject)
                .assignee(testAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(savedTask.getCreatedAt())
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .build();
        TaskResponse updatedResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), newPriority.name(),
                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, priorityChangeRequest);

        // Then
        assertEquals(newPriority.name(), result.priority());
        verify(taskRepository).save(argThat(task -> newPriority.equals(task.getPriority()))); // Verify priority changed
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void updateTask_changeAssignee_updatesAssigneeAndSetsHasChanges() {
        // Given
        Long newAssigneeId = 40L;
        User newAssignee = User.builder().id(newAssigneeId).username("newAssignee").build();
        UpdateTaskRequest assigneeChangeRequest = new UpdateTaskRequest(
                null, null, null, null, newAssigneeId, null
        );
        Task updatedTask = Task.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .project(testProject)
                .assignee(newAssignee)
                .reporter(testReporter)
                .dueDate(testDueDate)
                .createdAt(savedTask.getCreatedAt())
                .updatedAt(LocalDateTime.now().plusSeconds(1))
                .build();
        TaskResponse updatedResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testProject.getName(), newAssigneeId, newAssignee.getUsername(),
                testReporterId, testReporter.getUsername(), testDueDate,
                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(userService.getUserEntityById(newAssigneeId)).thenReturn(newAssignee);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, assigneeChangeRequest);

        // Then
        assertEquals(newAssigneeId, result.assigneeId());
        assertEquals("newAssignee", result.assigneeUsername());
        verify(userService).getUserEntityById(newAssigneeId);
        verify(taskRepository).save(argThat(task -> newAssignee.equals(task.getAssignee()))); // Verify assignee changed
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void deleteTask_success() {
        // Given
        when(taskRepository.existsById(testId)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(testId);

        // When
        taskService.deleteTask(testId);

        // Then
        verify(taskRepository).existsById(testId);
        verify(taskRepository).deleteById(testId);
    }

    @Test
    void deleteTask_notFound_throwsException() {
        // Given
        when(taskRepository.existsById(testId)).thenReturn(false);

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.deleteTask(testId));
        assertEquals("Task not found: ID " + testId, exception.getMessage());
        verify(taskRepository, never()).deleteById(testId);
    }

    @Test
    void getAllTasks_pagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> mockPage = new PageImpl<>(List.of(savedTask));
        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));

        when(taskRepository.findAll(pageable)).thenReturn(mockPage);
        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<TaskResponse> result = taskService.getAllTasks(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(taskRepository).findAll(pageable);
        verify(taskMapper).toTaskResponsePage(mockPage);
    }

    @Test
    void saveTask_success() {
        // When
        taskService.saveTask(savedTask);

        // Then
        verify(taskRepository).save(savedTask);
    }

    @Test
    void saveTask_nullTask_doesNotSave() {
        // Given
        Task nullTask = null;

        // When
        taskService.saveTask(nullTask);

        // Then
        verify(taskRepository, never()).save(any(Task.class));
    }
}