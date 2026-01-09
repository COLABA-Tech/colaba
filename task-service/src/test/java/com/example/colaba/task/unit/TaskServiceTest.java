//package com.example.colaba.task.unit;
//
//import com.example.colaba.shared.client.ProjectServiceClient;
//import com.example.colaba.shared.client.UserServiceClient;
//import com.example.colaba.shared.dto.task.CreateTaskRequest;
//import com.example.colaba.shared.dto.task.TaskResponse;
//import com.example.colaba.shared.dto.task.UpdateTaskRequest;
//import com.example.colaba.shared.entity.Project;
//import com.example.colaba.user.entity.User;
//import com.example.colaba.user.entity.UserJpa;
//import com.example.colaba.shared.entity.task.Task;
//import com.example.colaba.shared.entity.task.TaskPriority;
//import com.example.colaba.shared.entity.task.TaskStatus;
//import com.example.colaba.shared.exception.project.ProjectNotFoundException;
//import com.example.colaba.shared.exception.task.TaskNotFoundException;
//import com.example.colaba.shared.exception.user.UserNotFoundException;
//import com.example.colaba.task.mapper.TaskMapper;
//import com.example.colaba.user.mapper.UserMapper;
//import com.example.colaba.task.repository.TaskRepository;
//import com.example.colaba.task.service.TaskService;
//import feign.FeignException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//import java.time.LocalDate;
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TaskServiceTest {
//
//    @Mock
//    private TaskRepository taskRepository;
//
//    @Mock
//    private ProjectServiceClient projectServiceClient;
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @Mock
//    private TaskMapper taskMapper;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @InjectMocks
//    private TaskService taskService;
//
//    private CreateTaskRequest request;
//    private UpdateTaskRequest updateRequest;
//    private Task savedTask;
//    private TaskResponse taskResponse;
//
//    private final Long testId = 1L;
//    private final String testTitle = "Test Task";
//    private final String testDescription = "Test Description";
//    private final TaskStatus testStatus = TaskStatus.TODO;
//    private final TaskPriority testPriority = TaskPriority.LOW;
//    private final Long testProjectId = 1L;
//    private final Long testAssigneeId = 2L;
//    private final Long testReporterId = 1L;
//    private final LocalDate testDueDate = LocalDate.now();
//
//    private Project testProject;
//    private User testAssignee;
//    private User testReporter;
//    private UserJpa testAssigneeJpa;
//    private UserJpa testReporterJpa;
//
//    @BeforeEach
//    void setUp() {
//        testAssignee = User.builder().id(testAssigneeId).username("assignee").build();
//        testReporter = User.builder().id(testReporterId).username("reporter").build();
//
//        testAssigneeJpa = UserJpa.builder().id(testAssigneeId).username("assignee").build();
//        testReporterJpa = UserJpa.builder().id(testReporterId).username("reporter").build();
//
//        testProject = Project.builder()
//                .id(testProjectId)
//                .name("Test Project")
//                .description("Test Project Description")
//                .owner(testReporterJpa)
//                .build();
//
//        savedTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(OffsetDateTime.now())
//                .updatedAt(OffsetDateTime.now())
//                .build();
//
//        taskResponse = new TaskResponse(
//                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        request = new CreateTaskRequest(
//                testTitle, testDescription, testStatus, testPriority,
//                testProjectId, testAssigneeId, testReporterId, testDueDate
//        );
//
//        updateRequest = new UpdateTaskRequest(
//                "Updated Title", null, TaskStatus.IN_PROGRESS, null,
//                testAssigneeId, testDueDate.plusDays(1)
//        );
//    }
//
//    @Test
//    void createTask_success() {
//        // Given (arrange)
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
//        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);
//
//        // When (act)
//        TaskResponse result = taskService.createTask(request);
//
//        // Then (assert)
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        assertEquals(testStatus.name(), result.status());
//        assertEquals(testProjectId, result.projectId());
//        assertEquals(testAssigneeId, result.assigneeId());
//        assertEquals(testReporterId, result.reporterId());
//        verify(projectServiceClient).getProjectEntityById(testProjectId);
//        verify(userServiceClient).getUserEntityById(testReporterId);
//        verify(userServiceClient).getUserEntityById(testAssigneeId);
//        verify(taskRepository).save(any(Task.class));
//        verify(taskMapper).toTaskResponse(savedTask);
//    }
//
//    @Test
//    void createTask_projectNotFound_throwsException() {
//        // Given
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenThrow(feignException);
//
//        // When & Then
//        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
//                () -> taskService.createTask(request));
//        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
//        verify(userServiceClient, never()).getUserEntityById(anyLong());
//        verify(taskRepository, never()).save(any(Task.class));
//    }
//
//    @Test
//    void createTask_reporterNotFound_throwsException() {
//        // Given
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenThrow(feignException);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//
//        // When & Then
//        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
//                () -> taskService.createTask(request));
//        assertEquals("User not found: ID " + testReporterId, exception.getMessage());
//        verify(userServiceClient).getUserEntityById(testReporterId);
//        verify(taskRepository, never()).save(any(Task.class));
//    }
//
//    @Test
//    void createTask_assigneeOptional_nullAssignee() {
//        // Given
//        CreateTaskRequest optionalRequest = new CreateTaskRequest(
//                testTitle, testDescription, testStatus, testPriority,
//                testProjectId, null, testReporterId, testDueDate
//        );
//
//        Task nullAssigneeTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(null)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .build();
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(nullAssigneeTask);
//        when(taskMapper.toTaskResponse(nullAssigneeTask)).thenReturn(taskResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(optionalRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        verify(userServiceClient).getUserEntityById(testReporterId);
//        verify(userServiceClient, never()).getUserEntityById(testAssigneeId);
//        verify(taskRepository).save(any(Task.class));
//    }
//
//    @Test
//    void createTask_withNullPriority_setsPriorityToNull() {
//        // Given
//        CreateTaskRequest nullPriorityRequest = new CreateTaskRequest(
//                testTitle, testDescription, testStatus, null,
//                testProjectId, testAssigneeId, testReporterId, testDueDate
//        );
//
//        Task nullPriorityTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(null)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(OffsetDateTime.now())
//                .updatedAt(OffsetDateTime.now())
//                .build();
//
//        TaskResponse nullPriorityResponse = new TaskResponse(
//                testId, testTitle, testDescription, testStatus.name(), null,
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(nullPriorityTask);
//        when(taskMapper.toTaskResponse(nullPriorityTask)).thenReturn(nullPriorityResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(nullPriorityRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        assertNull(result.priority());
//        verify(projectServiceClient).getProjectEntityById(testProjectId);
//        verify(userServiceClient).getUserEntityById(testReporterId);
//        verify(userServiceClient).getUserEntityById(testAssigneeId);
//        verify(taskRepository).save(argThat(task -> task.getPriority() == null));
//        verify(taskMapper).toTaskResponse(nullPriorityTask);
//    }
//
//    @Test
//    void createTask_withNullStatus_setsDefaultStatus() {
//        // Given
//        CreateTaskRequest nullStatusRequest = new CreateTaskRequest(
//                testTitle, testDescription, null, testPriority,
//                testProjectId, testAssigneeId, testReporterId, testDueDate
//        );
//
//        Task defaultStatusTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(TaskStatus.getDefault())
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(OffsetDateTime.now())
//                .updatedAt(OffsetDateTime.now())
//                .build();
//
//        TaskResponse defaultStatusResponse = new TaskResponse(
//                testId, testTitle, testDescription, TaskStatus.getDefault().name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(defaultStatusTask);
//        when(taskMapper.toTaskResponse(defaultStatusTask)).thenReturn(defaultStatusResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(nullStatusRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        assertEquals(TaskStatus.getDefault().name(), result.status());
//        verify(taskRepository).save(argThat(task -> TaskStatus.getDefault().equals(task.getStatus())));
//        verify(taskMapper).toTaskResponse(defaultStatusTask);
//    }
//
//    @Test
//    void createTask_withNullDueDate_success() {
//        // Given
//        CreateTaskRequest nullDueDateRequest = new CreateTaskRequest(
//                testTitle, testDescription, testStatus, testPriority,
//                testProjectId, testAssigneeId, testReporterId, null
//        );
//
//        Task nullDueDateTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(null)
//                .createdAt(OffsetDateTime.now())
//                .updatedAt(OffsetDateTime.now())
//                .build();
//
//        TaskResponse nullDueDateResponse = new TaskResponse(
//                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), null,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(nullDueDateTask);
//        when(taskMapper.toTaskResponse(nullDueDateTask)).thenReturn(nullDueDateResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(nullDueDateRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        assertNull(result.dueDate());
//        verify(taskRepository).save(argThat(task -> task.getDueDate() == null));
//        verify(taskMapper).toTaskResponse(nullDueDateTask);
//    }
//
//    @Test
//    void createTask_withNullDescription_success() {
//        // Given
//        CreateTaskRequest nullDescriptionRequest = new CreateTaskRequest(
//                testTitle, null, testStatus, testPriority,
//                testProjectId, testAssigneeId, testReporterId, testDueDate
//        );
//
//        Task nullDescriptionTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(null)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(OffsetDateTime.now())
//                .updatedAt(OffsetDateTime.now())
//                .build();
//
//        TaskResponse nullDescriptionResponse = new TaskResponse(
//                testId, testTitle, null, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(nullDescriptionTask);
//        when(taskMapper.toTaskResponse(nullDescriptionTask)).thenReturn(nullDescriptionResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(nullDescriptionRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        assertNull(result.description());
//        verify(taskRepository).save(argThat(task -> task.getDescription() == null));
//        verify(taskMapper).toTaskResponse(nullDescriptionTask);
//    }
//
//    @Test
//    void createTask_assigneeAndReporterSameUser_success() {
//        // Given
//        Long sameUserId = testReporterId;
//        CreateTaskRequest sameUserRequest = new CreateTaskRequest(
//                testTitle, testDescription, testStatus, testPriority,
//                testProjectId, sameUserId, sameUserId, testDueDate
//        );
//
//        Task sameUserTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testReporterJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(OffsetDateTime.now())
//                .updatedAt(OffsetDateTime.now())
//                .build();
//
//        TaskResponse sameUserResponse = new TaskResponse(
//                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), sameUserId, testReporter.getUsername(),
//                sameUserId, testReporter.getUsername(), testDueDate,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(sameUserId)).thenReturn(testReporter);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(sameUserTask);
//        when(taskMapper.toTaskResponse(sameUserTask)).thenReturn(sameUserResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(sameUserRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(sameUserId, result.assigneeId());
//        assertEquals(sameUserId, result.reporterId());
//        verify(userServiceClient, times(2)).getUserEntityById(sameUserId);
//        verify(userMapper, times(2)).toUserJpa(testReporter);
//        verify(taskRepository).save(any(Task.class));
//    }
//
//    @Test
//    void createTask_verifyAllParametersPassedToBuilder() {
//        // Given
//        CreateTaskRequest fullRequest = new CreateTaskRequest(
//                "Special Title", "Detailed Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
//                testProjectId, testAssigneeId, testReporterId, LocalDate.now().plusDays(7)
//        );
//
//        Task expectedTask = Task.builder()
//                .title("Special Title")
//                .description("Detailed Description")
//                .status(TaskStatus.IN_PROGRESS)
//                .priority(TaskPriority.HIGH)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(LocalDate.now().plusDays(7))
//                .build();
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(userServiceClient.getUserEntityById(testReporterId)).thenReturn(testReporter);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testReporter)).thenReturn(testReporterJpa);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.save(argThat(task ->
//                "Special Title".equals(task.getTitle()) &&
//                        "Detailed Description".equals(task.getDescription()) &&
//                        TaskStatus.IN_PROGRESS.equals(task.getStatus()) &&
//                        TaskPriority.HIGH.equals(task.getPriority()) &&
//                        testProject.equals(task.getProject()) &&
//                        testAssigneeJpa.equals(task.getAssignee()) &&
//                        testReporterJpa.equals(task.getReporter()) &&
//                        LocalDate.now().plusDays(7).equals(task.getDueDate())
//        ))).thenReturn(savedTask);
//        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);
//
//        // When
//        TaskResponse result = taskService.createTask(fullRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        verify(taskRepository).save(any(Task.class));
//    }
//
//    @Test
//    void getTaskById_success() {
//        // Given (arrange)
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);
//
//        // When (act)
//        TaskResponse result = taskService.getTaskById(testId);
//
//        // Then (assert)
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        assertEquals(testProjectId, result.projectId());
//        verify(taskRepository).findById(testId);
//        verify(taskMapper).toTaskResponse(savedTask);
//    }
//
//    @Test
//    void getTaskById_notFound_throwsException() {
//        // Given (arrange)
//        when(taskRepository.findById(testId)).thenReturn(Optional.empty());
//
//        // When & Then
//        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
//                () -> taskService.getTaskById(testId));
//        assertEquals("Task not found: ID " + testId, exception.getMessage());
//        verify(taskMapper, never()).toTaskResponse(any(Task.class));
//    }
//
//    @Test
//    void getTasksByProject_success() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Task> mockPage = new PageImpl<>(List.of(savedTask));
//        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));
//
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenReturn(testProject);
//        when(taskRepository.findByProject(testProject, pageable)).thenReturn(mockPage);
//        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);
//
//        // When
//        Page<TaskResponse> result = taskService.getTasksByProject(testProjectId, pageable);
//
//        // Then
//        assertEquals(1, result.getContent().size());
//        verify(projectServiceClient).getProjectEntityById(testProjectId);
//        verify(taskRepository).findByProject(testProject, pageable);
//        verify(taskMapper).toTaskResponsePage(mockPage);
//    }
//
//    @Test
//    void getTasksByProject_projectNotFound_throwsException() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(projectServiceClient.getProjectEntityById(testProjectId)).thenThrow(feignException);
//
//        // When & Then
//        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
//                () -> taskService.getTasksByProject(testProjectId, pageable));
//        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
//        verify(taskRepository, never()).findByProject(any(Project.class), any(Pageable.class));
//    }
//
//    @Test
//    void getTasksByAssignee_success() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Task> mockPage = new PageImpl<>(List.of(savedTask));
//        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));
//
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenReturn(testAssignee);
//        when(userMapper.toUserJpa(testAssignee)).thenReturn(testAssigneeJpa);
//        when(taskRepository.findByAssignee(testAssigneeJpa, pageable)).thenReturn(mockPage);
//        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);
//
//        // When
//        Page<TaskResponse> result = taskService.getTasksByAssignee(testAssigneeId, pageable);
//
//        // Then
//        assertEquals(1, result.getContent().size());
//        verify(userServiceClient).getUserEntityById(testAssigneeId);
//        verify(userMapper).toUserJpa(testAssignee);
//        verify(taskRepository).findByAssignee(testAssigneeJpa, pageable);
//        verify(taskMapper).toTaskResponsePage(mockPage);
//    }
//
//    @Test
//    void getTasksByAssignee_userNotFound_throwsException() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(userServiceClient.getUserEntityById(testAssigneeId)).thenThrow(feignException);
//
//        // When & Then
//        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
//                () -> taskService.getTasksByAssignee(testAssigneeId, pageable));
//        assertEquals("User not found: ID " + testAssigneeId, exception.getMessage());
//        verify(taskRepository, never()).findByAssignee(any(UserJpa.class), any(Pageable.class));
//    }
//
//    @Test
//    void updateTask_success() {
//        // Given
//        Long newAssigneeId = 3L;
//        User newAssignee = User.builder().id(newAssigneeId).username("newassignee").build();
//        UserJpa newAssigneeJpa = UserJpa.builder().id(newAssigneeId).username("newassignee").build();
//
//        UpdateTaskRequest updateRequest = new UpdateTaskRequest(
//                "Updated Title", null, TaskStatus.IN_PROGRESS, null,
//                newAssigneeId, testDueDate.plusDays(1)  // Используем нового assignee
//        );
//
//        Task updatedTask = Task.builder()
//                .id(testId)
//                .title(updateRequest.title())
//                .status(updateRequest.status())
//                .assignee(newAssigneeJpa)
//                .dueDate(updateRequest.dueDate())
//                .build();
//
//        TaskResponse updatedResponse = new TaskResponse(
//                testId, updateRequest.title(), testDescription, updateRequest.status().name(), testPriority.name(),
//                testProjectId, testProject.getName(), newAssigneeId, "newassignee",
//                testReporterId, testReporter.getUsername(), updateRequest.dueDate(),
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(userServiceClient.getUserEntityById(newAssigneeId)).thenReturn(newAssignee);
//        when(userMapper.toUserJpa(newAssignee)).thenReturn(newAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
//        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, updateRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(updateRequest.title(), result.title());
//        assertEquals(updateRequest.status().name(), result.status());
//        verify(taskRepository).findById(testId);
//        verify(userServiceClient).getUserEntityById(newAssigneeId);
//        verify(taskRepository).save(any(Task.class));
//        verify(taskMapper).toTaskResponse(updatedTask);
//    }
//
//    @Test
//    void updateTask_noChanges_returnsUnchangedTask() {
//        // Given
//        UpdateTaskRequest noChangeRequest = new UpdateTaskRequest(
//                testTitle, testDescription, testStatus, testPriority, null, testDueDate
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, noChangeRequest);
//
//        // Then
//        assertEquals(testId, result.id());
//        assertEquals(testTitle, result.title());
//        verify(taskRepository).findById(testId);
//        verify(taskRepository, never()).save(any(Task.class));
//        verify(taskMapper).toTaskResponse(savedTask);
//    }
//
//    @Test
//    void updateTask_partialUpdate_ignoresNullFields() {
//        // Given
//        UpdateTaskRequest partialRequest = new UpdateTaskRequest(
//                "Partial Title", null, null, null, null, null
//        );
//        Task partialUpdatedTask = Task.builder()
//                .id(testId)
//                .title("Partial Title")
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .build();
//        TaskResponse partialResponse = new TaskResponse(
//                testId, "Partial Title", testDescription, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                OffsetDateTime.now(), OffsetDateTime.now()
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(taskRepository.save(any(Task.class))).thenReturn(partialUpdatedTask);
//        when(taskMapper.toTaskResponse(partialUpdatedTask)).thenReturn(partialResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, partialRequest);
//
//        // Then
//        assertEquals("Partial Title", result.title());
//        assertEquals(testStatus.name(), result.status());
//        verify(taskRepository).save(any(Task.class));
//    }
//
//    @Test
//    void updateTask_notFound_throwsException() {
//        // Given
//        when(taskRepository.findById(testId)).thenReturn(Optional.empty());
//
//        // When & Then
//        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
//                () -> taskService.updateTask(testId, updateRequest));
//        assertEquals("Task not found: ID " + testId, exception.getMessage());
//        verify(taskRepository, never()).save(any(Task.class));
//        verify(userServiceClient, never()).getUserEntityById(anyLong());
//        verify(taskMapper, never()).toTaskResponse(any(Task.class));
//    }
//
//    @Test
//    void updateTask_changeTitle_updatesTitleAndSetsHasChanges() {
//        // Given
//        UpdateTaskRequest titleChangeRequest = new UpdateTaskRequest(
//                "New Title", null, null, null, null, null
//        );
//        Task updatedTask = Task.builder()
//                .id(testId)
//                .title("New Title")
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(savedTask.getCreatedAt())
//                .updatedAt(OffsetDateTime.now().plusSeconds(1))
//                .build();
//        TaskResponse updatedResponse = new TaskResponse(
//                testId, "New Title", testDescription, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
//        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, titleChangeRequest);
//
//        // Then
//        assertEquals("New Title", result.title());
//        verify(taskRepository).save(argThat(task -> "New Title".equals(task.getTitle())));
//        verify(taskMapper).toTaskResponse(updatedTask);
//    }
//
//    @Test
//    void updateTask_changeDescription_updatesDescriptionAndSetsHasChanges() {
//        // Given
//        UpdateTaskRequest descriptionChangeRequest = new UpdateTaskRequest(
//                null, "New Description", null, null, null, null
//        );
//        Task updatedTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description("New Description")
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(savedTask.getCreatedAt())
//                .updatedAt(OffsetDateTime.now().plusSeconds(1))
//                .build();
//        TaskResponse updatedResponse = new TaskResponse(
//                testId, testTitle, "New Description", testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
//        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, descriptionChangeRequest);
//
//        // Then
//        assertEquals("New Description", result.description());
//        verify(taskRepository).save(argThat(task -> "New Description".equals(task.getDescription())));
//        verify(taskMapper).toTaskResponse(updatedTask);
//    }
//
//    @Test
//    void updateTask_changePriority_updatesPriorityAndSetsHasChanges() {
//        // Given
//        TaskPriority newPriority = TaskPriority.MEDIUM;
//        UpdateTaskRequest priorityChangeRequest = new UpdateTaskRequest(
//                null, null, null, newPriority, null, null
//        );
//        Task updatedTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(newPriority)
//                .project(testProject)
//                .assignee(testAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(savedTask.getCreatedAt())
//                .updatedAt(OffsetDateTime.now().plusSeconds(1))
//                .build();
//        TaskResponse updatedResponse = new TaskResponse(
//                testId, testTitle, testDescription, testStatus.name(), newPriority.name(),
//                testProjectId, testProject.getName(), testAssigneeId, testAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
//        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, priorityChangeRequest);
//
//        // Then
//        assertEquals(newPriority.name(), result.priority());
//        verify(taskRepository).save(argThat(task -> newPriority.equals(task.getPriority())));
//        verify(taskMapper).toTaskResponse(updatedTask);
//    }
//
//    @Test
//    void updateTask_changeAssignee_updatesAssigneeAndSetsHasChanges() {
//        // Given
//        Long newAssigneeId = 40L;
//        User newAssignee = User.builder().id(newAssigneeId).username("newAssignee").build();
//        UserJpa newAssigneeJpa = UserJpa.builder().id(newAssigneeId).username("newAssignee").build();
//        UpdateTaskRequest assigneeChangeRequest = new UpdateTaskRequest(
//                null, null, null, null, newAssigneeId, null
//        );
//        Task updatedTask = Task.builder()
//                .id(testId)
//                .title(testTitle)
//                .description(testDescription)
//                .status(testStatus)
//                .priority(testPriority)
//                .project(testProject)
//                .assignee(newAssigneeJpa)
//                .reporter(testReporterJpa)
//                .dueDate(testDueDate)
//                .createdAt(savedTask.getCreatedAt())
//                .updatedAt(OffsetDateTime.now().plusSeconds(1))
//                .build();
//        TaskResponse updatedResponse = new TaskResponse(
//                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
//                testProjectId, testProject.getName(), newAssigneeId, newAssignee.getUsername(),
//                testReporterId, testReporter.getUsername(), testDueDate,
//                savedTask.getCreatedAt(), updatedTask.getUpdatedAt()
//        );
//
//        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
//        when(userServiceClient.getUserEntityById(newAssigneeId)).thenReturn(newAssignee);
//        when(userMapper.toUserJpa(newAssignee)).thenReturn(newAssigneeJpa);
//        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
//        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);
//
//        // When
//        TaskResponse result = taskService.updateTask(testId, assigneeChangeRequest);
//
//        // Then
//        assertEquals(newAssigneeId, result.assigneeId());
//        assertEquals("newAssignee", result.assigneeUsername());
//        verify(userServiceClient).getUserEntityById(newAssigneeId);
//        verify(taskRepository).save(argThat(task -> newAssigneeJpa.equals(task.getAssignee())));
//        verify(taskMapper).toTaskResponse(updatedTask);
//    }
//
//    @Test
//    void deleteTask_success() {
//        // Given
//        when(taskRepository.existsById(testId)).thenReturn(true);
//        doNothing().when(taskRepository).deleteById(testId);
//
//        // When
//        taskService.deleteTask(testId);
//
//        // Then
//        verify(taskRepository).existsById(testId);
//        verify(taskRepository).deleteById(testId);
//    }
//
//    @Test
//    void deleteTask_notFound_throwsException() {
//        // Given
//        when(taskRepository.existsById(testId)).thenReturn(false);
//
//        // When & Then
//        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
//                () -> taskService.deleteTask(testId));
//        assertEquals("Task not found: ID " + testId, exception.getMessage());
//        verify(taskRepository, never()).deleteById(testId);
//    }
//
//    @Test
//    void getAllTasks_pagination() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Task> mockPage = new PageImpl<>(List.of(savedTask));
//        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));
//
//        when(taskRepository.findAll(pageable)).thenReturn(mockPage);
//        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);
//
//        // When
//        Page<TaskResponse> result = taskService.getAllTasks(pageable);
//
//        // Then
//        assertEquals(1, result.getContent().size());
//        verify(taskRepository).findAll(pageable);
//        verify(taskMapper).toTaskResponsePage(mockPage);
//    }
//}