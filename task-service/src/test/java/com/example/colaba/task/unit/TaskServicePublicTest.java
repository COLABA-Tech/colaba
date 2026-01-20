package com.example.colaba.task.unit;

import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.tag.TagNotFoundException;
import com.example.colaba.shared.common.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webmvc.client.UserServiceClient;
import com.example.colaba.shared.webmvc.security.ProjectAccessChecker;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.entity.task.TaskJpa;
import com.example.colaba.task.entity.task.TaskPriority;
import com.example.colaba.task.entity.task.TaskStatus;
import com.example.colaba.task.service.TaskService;
import com.example.colaba.task.service.TaskServicePublic;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServicePublicTest {

    @Mock
    private ProjectAccessChecker accessChecker;

    @Mock
    private TaskService taskService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TaskServicePublic taskServicePublic;

    private final Long currentUserId = 1L;
    private final Long adminUserId = 999L;
    private final Long testTaskId = 1L;
    private final Long testProjectId = 1L;
    private final Long testAssigneeId = 2L;
    private final Long testTagId = 3L;

    private TaskJpa taskJpa;
    private TaskResponse taskResponse;
    private CreateTaskRequest createRequest;
    private UpdateTaskRequest updateRequest;
    private Pageable pageable;
    private Page<TaskResponse> taskPage;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        taskJpa = TaskJpa.builder()
                .id(testTaskId)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .projectId(testProjectId)
                .assigneeId(testAssigneeId)
                .reporterId(currentUserId)
                .dueDate(LocalDate.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        taskResponse = new TaskResponse(
                testTaskId, "Test Task", "Test Description", TaskStatus.TODO.name(), TaskPriority.LOW.name(),
                testProjectId, testAssigneeId, currentUserId, LocalDate.now(),
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        createRequest = new CreateTaskRequest(
                "New Task", "New Description", TaskStatus.TODO, TaskPriority.MEDIUM,
                testProjectId, testAssigneeId, LocalDate.now()
        );

        updateRequest = new UpdateTaskRequest(
                "Updated Task", "Updated Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH,
                testAssigneeId, LocalDate.now().plusDays(1)
        );

        taskPage = new PageImpl<>(List.of(taskResponse));
    }

    @Test
    void getAllTasks_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getAllTasks(pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> result = taskServicePublic.getAllTasks(pageable, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getAllTasks(pageable);
    }

    @Test
    void getAllTasks_nonAdminUser_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.getAllTasks(pageable, currentUserId));
        assertEquals("Required user role: ADMIN", exception.getMessage());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService, never()).getAllTasks(any(Pageable.class));
    }

    @Test
    void getTaskById_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        when(taskService.getTaskById(testTaskId)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.getTaskById(testTaskId, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(testTaskId, result.id());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker, never()).requireAnyRole(anyLong(), anyLong());
        verify(taskService).getTaskById(testTaskId);
    }

    @Test
    void getTaskById_nonAdminWithAccess_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAnyRole(testProjectId, currentUserId);
        when(taskService.getTaskById(testTaskId)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.getTaskById(testTaskId, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(testTaskId, result.id());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker).requireAnyRole(testProjectId, currentUserId);
        verify(taskService).getTaskById(testTaskId);
    }

    @Test
    void getTaskById_nonAdminWithoutAccess_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doThrow(new AccessDeniedException("Access denied"))
                .when(accessChecker).requireAnyRole(testProjectId, currentUserId);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.getTaskById(testTaskId, currentUserId));
        assertEquals("Access denied", exception.getMessage());
        verify(taskService, never()).getTaskById(testTaskId);
    }

    @Test
    void getTaskById_taskNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId))
                .thenThrow(new TaskNotFoundException(testTaskId));

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskServicePublic.getTaskById(testTaskId, currentUserId));
        assertEquals("Task not found: ID " + testTaskId, exception.getMessage());
        verify(accessChecker, never()).requireAnyRole(anyLong(), anyLong());
    }

    @Test
    void getTasksByProject_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTasksByProject(testProjectId, pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> result = taskServicePublic.getTasksByProject(testProjectId, pageable, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(accessChecker, never()).requireAnyRole(anyLong(), anyLong());
        verify(taskService).getTasksByProject(testProjectId, pageable);
    }

    @Test
    void getTasksByProject_nonAdminWithAccess_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        doNothing().when(accessChecker).requireAnyRole(testProjectId, currentUserId);
        when(taskService.getTasksByProject(testProjectId, pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> result = taskServicePublic.getTasksByProject(testProjectId, pageable, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(accessChecker).requireAnyRole(testProjectId, currentUserId);
        verify(taskService).getTasksByProject(testProjectId, pageable);
    }

    @Test
    void getTasksByProject_nonAdminWithoutAccess_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        doThrow(new AccessDeniedException("No access to project"))
                .when(accessChecker).requireAnyRole(testProjectId, currentUserId);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.getTasksByProject(testProjectId, pageable, currentUserId));
        assertEquals("No access to project", exception.getMessage());
        verify(taskService, never()).getTasksByProject(anyLong(), any(Pageable.class));
    }

    @Test
    void createTask_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.createTask(createRequest, adminUserId)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.createTask(createRequest, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(testTaskId, result.id());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService).createTask(createRequest, adminUserId);
    }

    @Test
    void createTask_nonAdminEditor_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        when(taskService.createTask(createRequest, currentUserId)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.createTask(createRequest, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(testTaskId, result.id());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        verify(taskService).createTask(createRequest, currentUserId);
    }

    @Test
    void createTask_nonAdminNonEditor_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        doThrow(new AccessDeniedException("Editor role required"))
                .when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.createTask(createRequest, currentUserId));
        assertEquals("Editor role required", exception.getMessage());
        verify(taskService, never()).createTask(any(), any());
    }

    @Test
    void createTask_projectNotFound_throwsException() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        when(taskService.createTask(createRequest, currentUserId))
                .thenThrow(new ProjectNotFoundException(testProjectId));

        // When & Then
        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
                () -> taskServicePublic.createTask(createRequest, currentUserId));
        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
    }

    @Test
    void updateTask_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        when(taskService.updateTask(testTaskId, updateRequest)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.updateTask(testTaskId, updateRequest, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(testTaskId, result.id());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService).updateTask(testTaskId, updateRequest);
    }

    @Test
    void updateTask_nonAdminEditor_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        when(taskService.updateTask(testTaskId, updateRequest)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.updateTask(testTaskId, updateRequest, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(testTaskId, result.id());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        verify(taskService).updateTask(testTaskId, updateRequest);
    }

    @Test
    void updateTask_nonAdminNonEditor_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doThrow(new AccessDeniedException("Editor role required"))
                .when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.updateTask(testTaskId, updateRequest, currentUserId));
        assertEquals("Editor role required", exception.getMessage());
        verify(taskService, never()).updateTask(anyLong(), any());
    }

    @Test
    void updateTask_taskNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId))
                .thenThrow(new TaskNotFoundException(testTaskId));

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskServicePublic.updateTask(testTaskId, updateRequest, currentUserId));
        assertEquals("Task not found: ID " + testTaskId, exception.getMessage());
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
    }

    @Test
    void deleteTask_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(taskService).deleteTask(testTaskId);

        // When
        taskServicePublic.deleteTask(testTaskId, adminUserId);

        // Then
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService).deleteTask(testTaskId);
    }

    @Test
    void deleteTask_nonAdminEditor_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        doNothing().when(taskService).deleteTask(testTaskId);

        // When
        taskServicePublic.deleteTask(testTaskId, currentUserId);

        // Then
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        verify(taskService).deleteTask(testTaskId);
    }

    @Test
    void deleteTask_nonAdminNonEditor_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doThrow(new AccessDeniedException("Editor role required"))
                .when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.deleteTask(testTaskId, currentUserId));
        assertEquals("Editor role required", exception.getMessage());
        verify(taskService, never()).deleteTask(anyLong());
    }

    @Test
    void deleteTask_taskNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId))
                .thenThrow(new TaskNotFoundException(testTaskId));

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskServicePublic.deleteTask(testTaskId, currentUserId));
        assertEquals("Task not found: ID " + testTaskId, exception.getMessage());
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService, never()).deleteTask(anyLong());
    }

    @Test
    void getTasksByAssignee_adminUser_viewingOthersTasks_success() {
        // Given
        Long otherUserId = 999L;
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTasksByAssignee(otherUserId, pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> result = taskServicePublic.getTasksByAssignee(otherUserId, pageable, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTasksByAssignee(otherUserId, pageable);
    }

    @Test
    void getTasksByAssignee_nonAdmin_viewingOwnTasks_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTasksByAssignee(currentUserId, pageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> result = taskServicePublic.getTasksByAssignee(currentUserId, pageable, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTasksByAssignee(currentUserId, pageable);
    }

    @Test
    void getTasksByAssignee_nonAdmin_viewingOthersTasks_throwsAccessDenied() {
        // Given
        Long otherUserId = 999L;
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskServicePublic.getTasksByAssignee(otherUserId, pageable, currentUserId));
        assertEquals("You can only view your own assigned tasks", exception.getMessage());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService, never()).getTasksByAssignee(anyLong(), any(Pageable.class));
    }

    @Test
    void getTasksByAssignee_assigneeNotFound_throwsException() {
        // Given
        Long nonExistentUserId = 999L;
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTasksByAssignee(nonExistentUserId, pageable))
                .thenThrow(new UserNotFoundException(nonExistentUserId));

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> taskServicePublic.getTasksByAssignee(nonExistentUserId, pageable, adminUserId));
        assertEquals("User not found: ID " + nonExistentUserId, exception.getMessage());
        verify(userServiceClient).isAdmin(adminUserId);
    }

    @Test
    void getTagsByTask_adminUser_success() {
        // Given
        List<TagResponse> tags = List.of(
                new TagResponse(testTagId, "Tag 1", testProjectId),
                new TagResponse(testTagId + 1, "Tag 2", testProjectId)
        );

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        when(taskService.getTagsByTask(testTaskId)).thenReturn(tags);

        // When
        List<TagResponse> result = taskServicePublic.getTagsByTask(testTaskId, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker, never()).requireAnyRole(anyLong(), anyLong());
        verify(taskService).getTagsByTask(testTaskId);
    }

    @Test
    void getTagsByTask_nonAdminWithAccess_success() {
        // Given
        List<TagResponse> tags = List.of(new TagResponse(testTagId, "Tag 1", testProjectId));

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAnyRole(testProjectId, currentUserId);
        when(taskService.getTagsByTask(testTaskId)).thenReturn(tags);

        // When
        List<TagResponse> result = taskServicePublic.getTagsByTask(testTaskId, currentUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker).requireAnyRole(testProjectId, currentUserId);
        verify(taskService).getTagsByTask(testTaskId);
    }

    @Test
    void getTagsByTask_noTags_returnsEmptyList() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAnyRole(testProjectId, currentUserId);
        when(taskService.getTagsByTask(testTaskId)).thenReturn(List.of());

        // When
        List<TagResponse> result = taskServicePublic.getTagsByTask(testTaskId, currentUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskService).getTagsByTask(testTaskId);
    }

    @Test
    void assignTagToTask_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(taskService).assignTagToTask(testTaskId, testTagId);

        // When
        taskServicePublic.assignTagToTask(testTaskId, testTagId, adminUserId);

        // Then
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService).assignTagToTask(testTaskId, testTagId);
    }

    @Test
    void assignTagToTask_nonAdminEditor_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        doNothing().when(taskService).assignTagToTask(testTaskId, testTagId);

        // When
        taskServicePublic.assignTagToTask(testTaskId, testTagId, currentUserId);

        // Then
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        verify(taskService).assignTagToTask(testTaskId, testTagId);
    }

    @Test
    void assignTagToTask_tagNotFound_throwsException() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        doThrow(new TagNotFoundException(testTagId))
                .when(taskService).assignTagToTask(testTaskId, testTagId);

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> taskServicePublic.assignTagToTask(testTaskId, testTagId, currentUserId));
        assertEquals("Tag not found: ID " + testTagId, exception.getMessage());
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
    }

    @Test
    void removeTagFromTask_adminUser_success() {
        // Given
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(taskService).removeTagFromTask(testTaskId, testTagId);

        // When
        taskServicePublic.removeTagFromTask(testTaskId, testTagId, adminUserId);

        // Then
        verify(userServiceClient).isAdmin(adminUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService).removeTagFromTask(testTaskId, testTagId);
    }

    @Test
    void removeTagFromTask_nonAdminEditor_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(taskJpa);
        doNothing().when(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        doNothing().when(taskService).removeTagFromTask(testTaskId, testTagId);

        // When
        taskServicePublic.removeTagFromTask(testTaskId, testTagId, currentUserId);

        // Then
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(testTaskId);
        verify(accessChecker).requireAtLeastEditor(testProjectId, currentUserId);
        verify(taskService).removeTagFromTask(testTaskId, testTagId);
    }

    @Test
    void removeTagFromTask_taskNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId))
                .thenThrow(new TaskNotFoundException(testTaskId));

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> taskServicePublic.removeTagFromTask(testTaskId, testTagId, currentUserId));
        assertEquals("Task not found: ID " + testTaskId, exception.getMessage());
        verify(accessChecker, never()).requireAtLeastEditor(anyLong(), anyLong());
        verify(taskService, never()).removeTagFromTask(anyLong(), anyLong());
    }

    @Test
    void getTaskById_taskServiceThrowsException_shouldPropagate() {
        // Given
        RuntimeException serviceException = new RuntimeException("Internal service error");
        when(taskService.getTaskEntityById(testTaskId)).thenThrow(serviceException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskServicePublic.getTaskById(testTaskId, adminUserId));
        assertEquals("Internal service error", exception.getMessage());
    }

    @Test
    void getAllTasks_withDifferentPageable_success() {
        // Given
        Pageable customPageable = PageRequest.of(2, 5);
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.getAllTasks(customPageable)).thenReturn(taskPage);

        // When
        Page<TaskResponse> result = taskServicePublic.getAllTasks(customPageable, adminUserId);

        // Then
        assertNotNull(result);
        verify(taskService).getAllTasks(customPageable);
    }

    @Test
    void createTask_withNullValues_success() {
        // Given
        CreateTaskRequest nullRequest = new CreateTaskRequest(
                null, null, null, null,
                null, null, null
        );

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(taskService.createTask(nullRequest, adminUserId)).thenReturn(taskResponse);

        // When
        TaskResponse result = taskServicePublic.createTask(nullRequest, adminUserId);

        // Then
        assertNotNull(result);
        verify(taskService).createTask(nullRequest, adminUserId);
    }
}