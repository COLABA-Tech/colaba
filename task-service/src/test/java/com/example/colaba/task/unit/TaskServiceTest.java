package com.example.colaba.task.unit;

import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.tag.TagNotFoundException;
import com.example.colaba.shared.common.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webmvc.circuit.ProjectServiceClientWrapper;
import com.example.colaba.shared.webmvc.circuit.UserServiceClientWrapper;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.entity.task.TaskJpa;
import com.example.colaba.task.entity.task.TaskPriority;
import com.example.colaba.task.entity.task.TaskStatus;
import com.example.colaba.task.mapper.TaskMapper;
import com.example.colaba.task.repository.CommentRepository;
import com.example.colaba.task.repository.TaskRepository;
import com.example.colaba.task.repository.TaskTagRepository;
import com.example.colaba.task.service.TaskService;
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
import java.time.OffsetDateTime;
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
    private TaskTagRepository taskTagRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ProjectServiceClientWrapper projectServiceClient;

    @Mock
    private UserServiceClientWrapper userServiceClient;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private CreateTaskRequest request;
    private UpdateTaskRequest updateRequest;
    private TaskJpa savedTask;
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

    @BeforeEach
    void setUp() {
        savedTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        taskResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testAssigneeId,
                testReporterId, testDueDate
        );

        request = new CreateTaskRequest(
                testTitle, testDescription, testStatus, testPriority,
                testProjectId, testAssigneeId, testDueDate
        );

        updateRequest = new UpdateTaskRequest(
                "Updated Title", null, TaskStatus.IN_PROGRESS, null,
                testAssigneeId, testDueDate.plusDays(1)
        );
    }

    @Test
    void createTask_success() {
        // Given (arrange)
        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(savedTask);
        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);

        // When (act)
        TaskResponse result = taskService.createTask(request, testReporterId);

        // Then (assert)
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertEquals(testStatus.name(), result.status());
        assertEquals(testProjectId, result.projectId());
        assertEquals(testAssigneeId, result.assigneeId());
        assertEquals(testReporterId, result.reporterId());
        verify(projectServiceClient).projectExists(testProjectId);
        verify(userServiceClient).userExists(testReporterId);
        verify(userServiceClient).userExists(testAssigneeId);
        verify(taskRepository).save(any(TaskJpa.class));
        verify(taskMapper).toTaskResponse(savedTask);
    }

    @Test
    void createTask_projectNotFound_throwsException() {
        // Given
        when(projectServiceClient.projectExists(testProjectId)).thenReturn(false);

        // When & Then
        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
                () -> taskService.createTask(request, testReporterId));
        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
        verify(userServiceClient, never()).userExists(anyLong());
        verify(taskRepository, never()).save(any(TaskJpa.class));
    }

    @Test
    void createTask_reporterNotFound_throwsException() {
        // Given
        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(false);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> taskService.createTask(request, testReporterId));
        assertEquals("User not found: ID " + testReporterId, exception.getMessage());
        verify(userServiceClient).userExists(testReporterId);
        verify(taskRepository, never()).save(any(TaskJpa.class));
    }

    @Test
    void createTask_assigneeOptional_nullAssignee() {
        // Given
        CreateTaskRequest optionalRequest = new CreateTaskRequest(
                testTitle, testDescription, testStatus, testPriority,
                testProjectId, null, testDueDate
        );

        TaskJpa nullAssigneeTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(null)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .build();

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(nullAssigneeTask);
        when(taskMapper.toTaskResponse(nullAssigneeTask)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.createTask(optionalRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        verify(userServiceClient).userExists(testReporterId);
        verify(userServiceClient, never()).userExists(testAssigneeId);
        verify(taskRepository).save(any(TaskJpa.class));
    }

    @Test
    void createTask_assigneeNotFound_throwsException() {
        // Given
        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(false);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> taskService.createTask(request, testReporterId));
        assertEquals("User not found: ID " + testAssigneeId, exception.getMessage());
        verify(userServiceClient).userExists(testAssigneeId);
        verify(taskRepository, never()).save(any(TaskJpa.class));
    }

    @Test
    void createTask_withNullPriority_setsPriorityToNull() {
        // Given
        CreateTaskRequest nullPriorityRequest = new CreateTaskRequest(
                testTitle, testDescription, testStatus, null,
                testProjectId, testAssigneeId, testDueDate
        );

        TaskJpa nullPriorityTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(null)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        TaskResponse nullPriorityResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), null,
                testProjectId, testAssigneeId,
                testReporterId, testDueDate
        );

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(nullPriorityTask);
        when(taskMapper.toTaskResponse(nullPriorityTask)).thenReturn(nullPriorityResponse);

        // When
        TaskResponse result = taskService.createTask(nullPriorityRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertNull(result.priority());
        verify(projectServiceClient).projectExists(testProjectId);
        verify(userServiceClient).userExists(testReporterId);
        verify(userServiceClient).userExists(testAssigneeId);
        verify(taskRepository).save(argThat(task -> task.getPriority() == null));
        verify(taskMapper).toTaskResponse(nullPriorityTask);
    }

    @Test
    void createTask_withNullStatus_setsDefaultStatus() {
        // Given
        CreateTaskRequest nullStatusRequest = new CreateTaskRequest(
                testTitle, testDescription, null, testPriority,
                testProjectId, testAssigneeId, testDueDate
        );

        TaskJpa defaultStatusTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(TaskStatus.getDefault())
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        TaskResponse defaultStatusResponse = new TaskResponse(
                testId, testTitle, testDescription, TaskStatus.getDefault().name(), testPriority.name(),
                testProjectId, testAssigneeId,
                testReporterId, testDueDate
        );

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(defaultStatusTask);
        when(taskMapper.toTaskResponse(defaultStatusTask)).thenReturn(defaultStatusResponse);

        // When
        TaskResponse result = taskService.createTask(nullStatusRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertEquals(TaskStatus.getDefault().name(), result.status());
        verify(taskRepository).save(argThat(task -> TaskStatus.getDefault().equals(task.getStatus())));
        verify(taskMapper).toTaskResponse(defaultStatusTask);
    }

    @Test
    void createTask_withNullDueDate_success() {
        // Given
        CreateTaskRequest nullDueDateRequest = new CreateTaskRequest(
                testTitle, testDescription, testStatus, testPriority,
                testProjectId, testAssigneeId, null
        );

        TaskJpa nullDueDateTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(testReporterId)
                .dueDate(null)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        TaskResponse nullDueDateResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testAssigneeId,
                testReporterId, null
        );

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(nullDueDateTask);
        when(taskMapper.toTaskResponse(nullDueDateTask)).thenReturn(nullDueDateResponse);

        // When
        TaskResponse result = taskService.createTask(nullDueDateRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertNull(result.dueDate());
        verify(taskRepository).save(argThat(task -> task.getDueDate() == null));
        verify(taskMapper).toTaskResponse(nullDueDateTask);
    }

    @Test
    void createTask_withNullDescription_success() {
        // Given
        CreateTaskRequest nullDescriptionRequest = new CreateTaskRequest(
                testTitle, null, testStatus, testPriority,
                testProjectId, testAssigneeId, testDueDate
        );

        TaskJpa nullDescriptionTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(null)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        TaskResponse nullDescriptionResponse = new TaskResponse(
                testId, testTitle, null, testStatus.name(), testPriority.name(),
                testProjectId, testAssigneeId,
                testReporterId, testDueDate
        );

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(nullDescriptionTask);
        when(taskMapper.toTaskResponse(nullDescriptionTask)).thenReturn(nullDescriptionResponse);

        // When
        TaskResponse result = taskService.createTask(nullDescriptionRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        assertNull(result.description());
        verify(taskRepository).save(argThat(task -> task.getDescription() == null));
        verify(taskMapper).toTaskResponse(nullDescriptionTask);
    }

    @Test
    void createTask_assigneeAndReporterSameUser_success() {
        // Given
        Long sameUserId = testReporterId;
        CreateTaskRequest sameUserRequest = new CreateTaskRequest(
                testTitle, testDescription, testStatus, testPriority,
                testProjectId, sameUserId, testDueDate
        );

        TaskJpa sameUserTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(sameUserId)
                .reporterId(sameUserId)
                .dueDate(testDueDate)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        TaskResponse sameUserResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
                testProjectId, sameUserId,
                sameUserId, testDueDate
        );

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(sameUserId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(sameUserTask);
        when(taskMapper.toTaskResponse(sameUserTask)).thenReturn(sameUserResponse);

        // When
        TaskResponse result = taskService.createTask(sameUserRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        assertEquals(sameUserId, result.assigneeId());
        assertEquals(sameUserId, result.reporterId());
        verify(userServiceClient, times(2)).userExists(sameUserId);
        verify(taskRepository).save(any(TaskJpa.class));
    }

    @Test
    void createTask_verifyAllParametersPassedToBuilder() {
        // Given
        CreateTaskRequest fullRequest = new CreateTaskRequest(
                "Special Title", "Detailed Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                testProjectId, testAssigneeId, LocalDate.now().plusDays(7)
        );

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(userServiceClient.userExists(testReporterId)).thenReturn(true);
        when(taskRepository.save(argThat(task ->
                "Special Title".equals(task.getTitle()) &&
                        "Detailed Description".equals(task.getDescription()) &&
                        TaskStatus.IN_PROGRESS.equals(task.getStatus()) &&
                        TaskPriority.HIGH.equals(task.getPriority()) &&
                        testProjectId.equals(task.getProjectId()) &&
                        testAssigneeId.equals(task.getAssigneeId()) &&
                        testReporterId.equals(task.getReporterId()) &&
                        LocalDate.now().plusDays(7).equals(task.getDueDate())
        ))).thenReturn(savedTask);
        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.createTask(fullRequest, testReporterId);

        // Then
        assertEquals(testId, result.id());
        verify(taskRepository).save(any(TaskJpa.class));
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
        verify(taskMapper, never()).toTaskResponse(any(TaskJpa.class));
    }

    @Test
    void getTasksByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaskJpa> mockPage = new PageImpl<>(List.of(savedTask));
        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));

        when(projectServiceClient.projectExists(testProjectId)).thenReturn(true);
        when(taskRepository.findByProjectId(testProjectId, pageable)).thenReturn(mockPage);
        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<TaskResponse> result = taskService.getTasksByProject(testProjectId, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(projectServiceClient).projectExists(testProjectId);
        verify(taskRepository).findByProjectId(testProjectId, pageable);
        verify(taskMapper).toTaskResponsePage(mockPage);
    }

    @Test
    void getTasksByProject_projectNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(projectServiceClient.projectExists(testProjectId)).thenReturn(false);

        // When & Then
        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
                () -> taskService.getTasksByProject(testProjectId, pageable));
        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
        verify(taskRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void getTasksByAssignee_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaskJpa> mockPage = new PageImpl<>(List.of(savedTask));
        Page<TaskResponse> mockResponsePage = new PageImpl<>(List.of(taskResponse));

        when(userServiceClient.userExists(testAssigneeId)).thenReturn(true);
        when(taskRepository.findByAssigneeId(testAssigneeId, pageable)).thenReturn(mockPage);
        when(taskMapper.toTaskResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<TaskResponse> result = taskService.getTasksByAssignee(testAssigneeId, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(userServiceClient).userExists(testAssigneeId);
        verify(taskRepository).findByAssigneeId(testAssigneeId, pageable);
        verify(taskMapper).toTaskResponsePage(mockPage);
    }

    @Test
    void getTasksByAssignee_userNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userServiceClient.userExists(testAssigneeId)).thenReturn(false);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> taskService.getTasksByAssignee(testAssigneeId, pageable));
        assertEquals("User not found: ID " + testAssigneeId, exception.getMessage());
        verify(taskRepository, never()).findByAssigneeId(anyLong(), any(Pageable.class));
    }

    @Test
    void updateTask_success() {
        // Given
        Long newAssigneeId = 3L;
        UpdateTaskRequest updateRequest = new UpdateTaskRequest(
                "Updated Title", null, TaskStatus.IN_PROGRESS, null,
                newAssigneeId, testDueDate.plusDays(1)
        );

        TaskJpa updatedTask = TaskJpa.builder()
                .id(testId)
                .title("Updated Title")
                .description(testDescription)
                .status(TaskStatus.IN_PROGRESS)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(newAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate.plusDays(1))
                .build();

        TaskResponse updatedResponse = new TaskResponse(
                testId, "Updated Title", testDescription, TaskStatus.IN_PROGRESS.name(), testPriority.name(),
                testProjectId, newAssigneeId,
                testReporterId, testDueDate.plusDays(1)
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(userServiceClient.userExists(newAssigneeId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, updateRequest);

        // Then
        assertEquals(testId, result.id());
        assertEquals("Updated Title", result.title());
        assertEquals(TaskStatus.IN_PROGRESS.name(), result.status());
        verify(taskRepository).findById(testId);
        verify(userServiceClient).userExists(newAssigneeId);
        verify(taskRepository).save(any(TaskJpa.class));
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void updateTask_noChanges_returnsUnchangedTask() {
        // Given
        UpdateTaskRequest noChangeRequest = new UpdateTaskRequest(
                testTitle, testDescription, testStatus, testPriority, testAssigneeId, testDueDate
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskMapper.toTaskResponse(savedTask)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, noChangeRequest);

        // Then
        assertEquals(testId, result.id());
        assertEquals(testTitle, result.title());
        verify(taskRepository).findById(testId);
        verify(taskRepository, never()).save(any(TaskJpa.class));
        verify(taskMapper).toTaskResponse(savedTask);
    }

    @Test
    void updateTask_partialUpdate_ignoresNullFields() {
        // Given
        UpdateTaskRequest partialRequest = new UpdateTaskRequest(
                "Partial Title", null, null, null, null, null
        );
        TaskJpa partialUpdatedTask = TaskJpa.builder()
                .id(testId)
                .title("Partial Title")
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .build();
        TaskResponse partialResponse = new TaskResponse(
                testId, "Partial Title", testDescription, testStatus.name(), testPriority.name(),
                testProjectId, testAssigneeId,
                testReporterId, testDueDate
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(partialUpdatedTask);
        when(taskMapper.toTaskResponse(partialUpdatedTask)).thenReturn(partialResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, partialRequest);

        // Then
        assertEquals("Partial Title", result.title());
        assertEquals(testStatus.name(), result.status());
        verify(taskRepository).save(any(TaskJpa.class));
    }

    @Test
    void updateTask_notFound_throwsException() {
        // Given
        when(taskRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.updateTask(testId, updateRequest));
        assertEquals("Task not found: ID " + testId, exception.getMessage());
        verify(taskRepository, never()).save(any(TaskJpa.class));
        verify(userServiceClient, never()).userExists(anyLong());
        verify(taskMapper, never()).toTaskResponse(any(TaskJpa.class));
    }

    @Test
    void updateTask_changeAssignee_updatesAssigneeAndSetsHasChanges() {
        // Given
        Long newAssigneeId = 40L;
        UpdateTaskRequest assigneeChangeRequest = new UpdateTaskRequest(
                null, null, null, null, newAssigneeId, null
        );
        TaskJpa updatedTask = TaskJpa.builder()
                .id(testId)
                .title(testTitle)
                .description(testDescription)
                .status(testStatus)
                .priority(testPriority)
                .projectId(testProjectId)
                .assigneeId(newAssigneeId)
                .reporterId(testReporterId)
                .dueDate(testDueDate)
                .build();
        TaskResponse updatedResponse = new TaskResponse(
                testId, testTitle, testDescription, testStatus.name(), testPriority.name(),
                testProjectId, newAssigneeId,
                testReporterId, testDueDate
        );

        when(taskRepository.findById(testId)).thenReturn(Optional.of(savedTask));
        when(userServiceClient.userExists(newAssigneeId)).thenReturn(true);
        when(taskRepository.save(any(TaskJpa.class))).thenReturn(updatedTask);
        when(taskMapper.toTaskResponse(updatedTask)).thenReturn(updatedResponse);

        // When
        TaskResponse result = taskService.updateTask(testId, assigneeChangeRequest);

        // Then
        assertEquals(newAssigneeId, result.assigneeId());
        verify(userServiceClient).userExists(newAssigneeId);
        verify(taskRepository).save(argThat(task -> newAssigneeId.equals(task.getAssigneeId())));
        verify(taskMapper).toTaskResponse(updatedTask);
    }

    @Test
    void deleteTask_success() {
        // Given
        when(taskRepository.existsById(testId)).thenReturn(true);

        // When
        taskService.deleteTask(testId);

        // Then
        verify(taskRepository).existsById(testId);
        verify(taskTagRepository).deleteByTaskId(testId);
        verify(commentRepository).deleteByTaskId(testId);
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
        Page<TaskJpa> mockPage = new PageImpl<>(List.of(savedTask));
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
    void assignTagToTask_success() {
        // Given
        Long taskId = 1L;
        Long tagId = 1L;
        Long projectId = 1L;

        TaskJpa task = TaskJpa.builder()
                .id(taskId)
                .projectId(projectId)
                .build();

        TagResponse tag = new TagResponse(tagId, "Test Tag", projectId);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectServiceClient.getTagById(tagId)).thenReturn(tag);
        when(taskTagRepository.existsByTaskIdAndTagId(taskId, tagId)).thenReturn(false);

        // When
        taskService.assignTagToTask(taskId, tagId);

        // Then
        verify(taskRepository).findById(taskId);
        verify(projectServiceClient).getTagById(tagId);
        verify(taskTagRepository).existsByTaskIdAndTagId(taskId, tagId);
        verify(taskTagRepository).save(any());
    }

    @Test
    void assignTagToTask_tagNotFound_throwsException() {
        // Given
        Long taskId = 1L;
        Long tagId = 1L;

        TaskJpa task = TaskJpa.builder()
                .id(taskId)
                .projectId(1L)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectServiceClient.getTagById(tagId)).thenReturn(null);

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> taskService.assignTagToTask(taskId, tagId));
        assertEquals("Tag not found: ID " + tagId, exception.getMessage());
        verify(taskTagRepository, never()).save(any());
    }

    @Test
    void removeTagFromTask_success() {
        // Given
        Long taskId = 1L;
        Long tagId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(true);

        // When
        taskService.removeTagFromTask(taskId, tagId);

        // Then
        verify(taskRepository).existsById(taskId);
        verify(taskTagRepository).deleteByTaskIdAndTagId(taskId, tagId);
    }

    @Test
    void removeTagFromTask_taskNotFound_throwsException() {
        // Given
        Long taskId = 1L;
        Long tagId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(false);

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.removeTagFromTask(taskId, tagId));
        assertEquals("Task not found: ID " + taskId, exception.getMessage());
        verify(taskTagRepository, never()).deleteByTaskIdAndTagId(anyLong(), anyLong());
    }

    @Test
    void handleUserDeletion_success() {
        // Given
        Long userId = 1L;

        // When
        taskService.handleUserDeletion(userId);

        // Then
        verify(taskRepository).setReporterIdToNullByReporterId(userId);
        verify(taskRepository).setAssigneeIdToNullByAssigneeId(userId);
        verify(commentRepository).deleteByUserId(userId);
    }

    @Test
    void deleteTasksByProject_success() {
        // Given
        Long projectId = 1L;
        TaskJpa task1 = TaskJpa.builder().id(1L).build();
        TaskJpa task2 = TaskJpa.builder().id(2L).build();

        when(taskRepository.findAllByProjectId(projectId)).thenReturn(List.of(task1, task2));
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.existsById(2L)).thenReturn(true);

        // When
        taskService.deleteTasksByProject(projectId);

        // Then
        verify(taskRepository).findAllByProjectId(projectId);
        verify(taskRepository, times(2)).existsById(anyLong());
        verify(taskTagRepository, times(2)).deleteByTaskId(anyLong());
        verify(commentRepository, times(2)).deleteByTaskId(anyLong());
        verify(taskRepository, times(2)).deleteById(anyLong());
    }

    @Test
    void getTagsByTask_success() {
        // Given
        Long taskId = 1L;
        List<Long> tagIds = List.of(1L, 2L);
        TagResponse tag1 = new TagResponse(1L, "Tag 1", 1L);
        TagResponse tag2 = new TagResponse(2L, "Tag 2", 1L);

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(taskTagRepository.findTagIdsByTaskId(taskId)).thenReturn(tagIds);
        when(projectServiceClient.getTagsByIds(tagIds)).thenReturn(List.of(tag1, tag2));

        // When
        List<TagResponse> result = taskService.getTagsByTask(taskId);

        // Then
        assertEquals(2, result.size());
        verify(taskRepository).existsById(taskId);
        verify(taskTagRepository).findTagIdsByTaskId(taskId);
        verify(projectServiceClient).getTagsByIds(tagIds);
    }

    @Test
    void getTagsByTask_noTags_returnsEmptyList() {
        // Given
        Long taskId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(taskTagRepository.findTagIdsByTaskId(taskId)).thenReturn(List.of());

        // When
        List<TagResponse> result = taskService.getTagsByTask(taskId);

        // Then
        assertTrue(result.isEmpty());
        verify(projectServiceClient, never()).getTagsByIds(any());
    }

    @Test
    void getTagsByTask_taskNotFound_throwsException() {
        // Given
        Long taskId = 1L;

        when(taskRepository.existsById(taskId)).thenReturn(false);

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskService.getTagsByTask(taskId));
        assertEquals("Task not found: ID " + taskId, exception.getMessage());
        verify(taskTagRepository, never()).findTagIdsByTaskId(anyLong());
    }
}