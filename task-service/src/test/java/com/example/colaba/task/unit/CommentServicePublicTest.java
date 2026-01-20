package com.example.colaba.task.unit;

import com.example.colaba.shared.webmvc.client.UserServiceClient;
import com.example.colaba.shared.webmvc.security.ProjectAccessChecker;
import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.dto.comment.CommentScrollResponse;
import com.example.colaba.task.dto.comment.CreateCommentRequest;
import com.example.colaba.task.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.entity.CommentJpa;
import com.example.colaba.task.entity.task.TaskJpa;
import com.example.colaba.task.service.CommentService;
import com.example.colaba.task.service.CommentServicePublic;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServicePublicTest {

    @Mock
    private ProjectAccessChecker accessChecker;

    @Mock
    private CommentService commentService;

    @Mock
    private TaskService taskService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CommentServicePublic commentServicePublic;

    private TaskJpa mockTask;
    private CommentJpa mockComment;
    private CommentResponse mockCommentResponse;
    private final Long currentUserId = 1L;
    private final Long otherUserId = 2L;
    private final Long adminUserId = 999L;

    @BeforeEach
    void setUp() {
        mockTask = TaskJpa.builder()
                .id(1L)
                .projectId(100L)
                .title("Test Task")
                .build();

        mockComment = CommentJpa.builder()
                .id(1L)
                .taskId(1L)
                .userId(currentUserId)
                .content("Test content")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        mockCommentResponse = new CommentResponse(
                1L, 1L, currentUserId, "Test content"
        );
    }

    @Test
    void createComment_ShouldSuccess_WhenUserIsAuthorAndHasAccess() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(1L, "Test content");

        when(taskService.getTaskEntityById(1L)).thenReturn(mockTask);
        doNothing().when(accessChecker).requireAnyRole(100L, currentUserId);
        when(commentService.createComment(request, currentUserId)).thenReturn(mockCommentResponse);

        // Act
        CommentResponse result = commentServicePublic.createComment(request, currentUserId);

        // Assert
        assertEquals(mockCommentResponse, result);
        verify(taskService).getTaskEntityById(1L);
        verify(accessChecker).requireAnyRole(100L, currentUserId);
        verify(commentService).createComment(request, currentUserId);
    }

    @Test
    void getCommentById_ShouldSuccess_WhenUserIsAdmin() {
        // Arrange
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(commentService.getCommentById(1L)).thenReturn(mockCommentResponse);

        // Act
        CommentResponse result = commentServicePublic.getCommentById(1L, adminUserId);

        // Assert
        assertEquals(mockCommentResponse, result);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(commentService).getCommentById(1L);
    }

    @Test
    void getCommentById_ShouldThrowAccessDenied_WhenUserIsNotAdmin() {
        // Arrange
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.getCommentById(1L, currentUserId));

        assertEquals("Required user role: ADMIN", exception.getMessage());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(commentService, never()).getCommentById(anyLong());
    }

    @Test
    void getCommentsByTask_ShouldSuccess_WhenUserIsAdmin() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> mockPage = new PageImpl<>(List.of(mockCommentResponse));

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(commentService.getCommentsByTask(1L, pageable)).thenReturn(mockPage);

        // Act
        Page<CommentResponse> result = commentServicePublic.getCommentsByTask(1L, pageable, adminUserId);

        // Assert
        assertEquals(mockPage, result);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(commentService).getCommentsByTask(1L, pageable);
        verify(accessChecker, never()).requireAnyRole(anyLong(), anyLong());
    }

    @Test
    void getCommentsByTask_ShouldSuccess_WhenUserHasProjectAccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> mockPage = new PageImpl<>(List.of(mockCommentResponse));

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(1L)).thenReturn(mockTask);
        doNothing().when(accessChecker).requireAnyRole(100L, currentUserId);
        when(commentService.getCommentsByTask(1L, pageable)).thenReturn(mockPage);

        // Act
        Page<CommentResponse> result = commentServicePublic.getCommentsByTask(1L, pageable, currentUserId);

        // Assert
        assertEquals(mockPage, result);
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(1L);
        verify(accessChecker).requireAnyRole(100L, currentUserId);
        verify(commentService).getCommentsByTask(1L, pageable);
    }

    @Test
    void getCommentsByTaskScroll_ShouldSuccess_WhenUserIsAdmin() {
        // Arrange
        String cursor = null;
        int limit = 10;
        CommentScrollResponse mockScrollResponse = new CommentScrollResponse(
                List.of(mockCommentResponse), "next-cursor", true
        );

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        when(commentService.getCommentsByTaskScroll(1L, cursor, limit)).thenReturn(mockScrollResponse);

        // Act
        CommentScrollResponse result = commentServicePublic.getCommentsByTaskScroll(1L, cursor, limit, adminUserId);

        // Assert
        assertEquals(mockScrollResponse, result);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(commentService).getCommentsByTaskScroll(1L, cursor, limit);
        verify(accessChecker, never()).requireAnyRole(anyLong(), anyLong());
    }

    @Test
    void getCommentsByTaskScroll_ShouldSuccess_WhenUserHasProjectAccess() {
        // Arrange
        String cursor = null;
        int limit = 10;
        CommentScrollResponse mockScrollResponse = new CommentScrollResponse(
                List.of(mockCommentResponse), "next-cursor", true
        );

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(1L)).thenReturn(mockTask);
        doNothing().when(accessChecker).requireAnyRole(100L, currentUserId);
        when(commentService.getCommentsByTaskScroll(1L, cursor, limit)).thenReturn(mockScrollResponse);

        // Act
        CommentScrollResponse result = commentServicePublic.getCommentsByTaskScroll(1L, cursor, limit, currentUserId);

        // Assert
        assertEquals(mockScrollResponse, result);
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(1L);
        verify(accessChecker).requireAnyRole(100L, currentUserId);
        verify(commentService).getCommentsByTaskScroll(1L, cursor, limit);
    }

    @Test
    void updateComment_ShouldSuccess_WhenUserIsCommentAuthor() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
        CommentResponse updatedResponse = new CommentResponse(
                1L, 1L, currentUserId, "Updated content"
        );

        when(commentService.getCommentEntityById(1L)).thenReturn(mockComment);
        when(commentService.updateComment(1L, request)).thenReturn(updatedResponse);

        // Act
        CommentResponse result = commentServicePublic.updateComment(1L, request, currentUserId);

        // Assert
        assertEquals(updatedResponse, result);
        verify(commentService).getCommentEntityById(1L);
        verify(commentService).updateComment(1L, request);
    }

    @Test
    void updateComment_ShouldThrowAccessDenied_WhenUserIsNotCommentAuthor() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");

        when(commentService.getCommentEntityById(1L)).thenReturn(mockComment);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.updateComment(1L, request, otherUserId));

        assertEquals("You can only update your own comments", exception.getMessage());
        verify(commentService).getCommentEntityById(1L);
        verify(commentService, never()).updateComment(anyLong(), any());
    }

    @Test
    void deleteComment_ShouldSuccess_WhenUserIsAdmin() {
        // Arrange
        when(commentService.getCommentEntityById(1L)).thenReturn(mockComment);
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        doNothing().when(commentService).deleteComment(1L);

        // Act
        commentServicePublic.deleteComment(1L, adminUserId);

        // Assert
        verify(commentService).getCommentEntityById(1L);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(commentService).deleteComment(1L);
    }

    @Test
    void deleteComment_ShouldSuccess_WhenUserIsCommentAuthor() {
        // Arrange
        when(commentService.getCommentEntityById(1L)).thenReturn(mockComment);
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        doNothing().when(commentService).deleteComment(1L);

        // Act
        commentServicePublic.deleteComment(1L, currentUserId);

        // Assert
        verify(commentService).getCommentEntityById(1L);
        verify(userServiceClient).isAdmin(currentUserId);
        verify(commentService).deleteComment(1L);
    }

    @Test
    void deleteComment_ShouldThrowAccessDenied_WhenUserIsNotAdminAndNotAuthor() {
        // Arrange
        CommentJpa otherUserComment = CommentJpa.builder()
                .id(2L)
                .taskId(1L)
                .userId(otherUserId)
                .content("Other user comment")
                .build();

        when(commentService.getCommentEntityById(2L)).thenReturn(otherUserComment);
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.deleteComment(2L, currentUserId));

        assertEquals("You can only delete your own comments", exception.getMessage());
        verify(commentService).getCommentEntityById(2L);
        verify(userServiceClient).isAdmin(currentUserId);
        verify(commentService, never()).deleteComment(anyLong());
    }

    @Test
    void deleteComment_ShouldThrowAccessDeniedWithCorrectMessage_WhenAuthorizationFails() {
        // Arrange
        CommentJpa otherUserComment = CommentJpa.builder()
                .id(2L)
                .taskId(1L)
                .userId(otherUserId)
                .content("Other user comment")
                .build();

        when(commentService.getCommentEntityById(2L)).thenReturn(otherUserComment);
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);

        // Act
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.deleteComment(2L, currentUserId));

        // Assert
        assertTrue(exception.getMessage().contains("You can only delete your own comments"));
    }

    @Test
    void getCommentsByTask_ShouldThrowAccessDenied_WhenUserHasNoProjectAccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(1L)).thenReturn(mockTask);
        doThrow(new AccessDeniedException("No access")).when(accessChecker).requireAnyRole(100L, currentUserId);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.getCommentsByTask(1L, pageable, currentUserId));

        assertEquals("No access", exception.getMessage());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(1L);
        verify(accessChecker).requireAnyRole(100L, currentUserId);
        verify(commentService, never()).getCommentsByTask(anyLong(), any());
    }

    @Test
    void createComment_ShouldThrowAccessDenied_WhenUserHasNoProjectAccess() {
        // Arrange
        CreateCommentRequest request = new CreateCommentRequest(1L, "Test content");

        when(taskService.getTaskEntityById(1L)).thenReturn(mockTask);
        doThrow(new AccessDeniedException("No project access")).when(accessChecker).requireAnyRole(100L, currentUserId);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.createComment(request, currentUserId));

        assertEquals("No project access", exception.getMessage());
        verify(taskService).getTaskEntityById(1L);
        verify(accessChecker).requireAnyRole(100L, currentUserId);
        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    void getCommentsByTaskScroll_ShouldThrowAccessDenied_WhenUserHasNoProjectAccess() {
        // Arrange
        String cursor = null;
        int limit = 10;

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(false);
        when(taskService.getTaskEntityById(1L)).thenReturn(mockTask);
        doThrow(new AccessDeniedException("Access denied")).when(accessChecker).requireAnyRole(100L, currentUserId);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentServicePublic.getCommentsByTaskScroll(1L, cursor, limit, currentUserId));

        assertEquals("Access denied", exception.getMessage());
        verify(userServiceClient).isAdmin(currentUserId);
        verify(taskService).getTaskEntityById(1L);
        verify(accessChecker).requireAnyRole(100L, currentUserId);
        verify(commentService, never()).getCommentsByTaskScroll(anyLong(), any(), anyInt());
    }

    @Test
    void updateComment_ShouldUseCommentEntityFromService() {
        // Arrange
        UpdateCommentRequest request = new UpdateCommentRequest("Updated");
        when(commentService.getCommentEntityById(1L)).thenReturn(mockComment);
        when(commentService.updateComment(1L, request)).thenReturn(mockCommentResponse);

        // Act
        commentServicePublic.updateComment(1L, request, currentUserId);

        // Assert
        verify(commentService).getCommentEntityById(1L);
        assertEquals(mockComment.getUserId(), currentUserId);
    }

    @Test
    void deleteComment_ShouldHandleAdminFlagCorrectly() {
        // Arrange
        CommentJpa adminDeletableComment = CommentJpa.builder()
                .id(3L)
                .userId(otherUserId)
                .build();

        when(commentService.getCommentEntityById(3L)).thenReturn(adminDeletableComment);
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(true);
        doNothing().when(commentService).deleteComment(3L);

        // Act
        commentServicePublic.deleteComment(3L, adminUserId);

        // Assert
        verify(userServiceClient).isAdmin(adminUserId);
        verify(commentService).deleteComment(3L);
    }
}