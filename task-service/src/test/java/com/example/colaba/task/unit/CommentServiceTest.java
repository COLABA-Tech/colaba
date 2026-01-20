package com.example.colaba.task.unit;

import com.example.colaba.shared.common.exception.comment.CommentNotFoundException;
import com.example.colaba.shared.common.exception.task.TaskNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webmvc.circuit.UserServiceClientWrapper;
import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.dto.comment.CommentScrollResponse;
import com.example.colaba.task.dto.comment.CreateCommentRequest;
import com.example.colaba.task.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.entity.CommentJpa;
import com.example.colaba.task.mapper.CommentMapper;
import com.example.colaba.task.repository.CommentRepository;
import com.example.colaba.task.repository.TaskRepository;
import com.example.colaba.task.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserServiceClientWrapper userServiceClient;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private CommentJpa mockComment;
    private CommentJpa mockComment2;
    private CommentResponse mockResponse;
    private OffsetDateTime fixedCreatedAt = OffsetDateTime.now();

    @BeforeEach
    void setUp() {
        fixedCreatedAt = OffsetDateTime.now();

        mockComment = CommentJpa.builder()
                .id(1L)
                .taskId(1L)
                .userId(1L)
                .content("Test content")
                .createdAt(fixedCreatedAt)
                .updatedAt(fixedCreatedAt)
                .build();

        mockComment2 = CommentJpa.builder()
                .id(2L)
                .taskId(1L)
                .userId(1L)
                .content("Test2")
                .createdAt(fixedCreatedAt.minusSeconds(1))
                .build();

        mockResponse = new CommentResponse(1L, 1L, 1L, "Test content");
    }

    @Test
    void createComment_ShouldReturnResponse_WhenValidRequest() {
        CreateCommentRequest request = new CreateCommentRequest(1L, "Test content");

        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.save(any(CommentJpa.class))).thenReturn(mockComment);
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse result = commentService.createComment(request, 1L);

        assertEquals(mockResponse, result);
        verify(userServiceClient).userExists(1L);
        verify(taskRepository).existsById(1L);
        verify(commentRepository).save(any(CommentJpa.class));
        verify(commentMapper).toResponse(mockComment);
    }

    @Test
    void createComment_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        CreateCommentRequest request = new CreateCommentRequest(1L, "Test");

        when(userServiceClient.userExists(999L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> commentService.createComment(request, 999L));
        assertEquals("User not found: ID " + 999L, exception.getMessage());

        verify(userServiceClient).userExists(999L);
        verify(taskRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).save(any(CommentJpa.class));
    }

    @Test
    void createComment_ShouldThrowTaskNotFoundException_WhenTaskNotExists() {
        CreateCommentRequest request = new CreateCommentRequest(999L, "Test");

        when(userServiceClient.userExists(1L)).thenReturn(true);
        when(taskRepository.existsById(999L)).thenReturn(false);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> commentService.createComment(request, 1L));
        assertEquals("Task not found: ID " + 999L, exception.getMessage());

        verify(userServiceClient).userExists(1L);
        verify(taskRepository).existsById(999L);
        verify(commentRepository, never()).save(any(CommentJpa.class));
    }

    @Test
    void getCommentById_ShouldReturnResponse_WhenCommentExists() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse result = commentService.getCommentById(1L);

        assertEquals(mockResponse, result);
    }

    @Test
    void getCommentById_ShouldThrowNotFound_WhenCommentNotExists() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.getCommentById(999L));
        assertEquals("Comment not found: 999", exception.getMessage());
    }

    @Test
    void getCommentsByTask_ShouldReturnPage_WhenValidTaskId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentJpa> mockPage = new PageImpl<>(List.of(mockComment));
        Page<CommentResponse> mockRespPage = new PageImpl<>(List.of(mockResponse));

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(mockPage);
        when(commentMapper.toResponsePage(mockPage)).thenReturn(mockRespPage);

        Page<CommentResponse> result = commentService.getCommentsByTask(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(taskRepository).existsById(1L);
    }

    @Test
    void getCommentsByTask_ShouldThrowTaskNotFoundException_WhenTaskNotExists() {
        Pageable pageable = PageRequest.of(0, 10);
        when(taskRepository.existsById(999L)).thenReturn(false);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> commentService.getCommentsByTask(999L, pageable));
        assertEquals("Task not found: ID " + 999L, exception.getMessage());

        verify(commentRepository, never()).findByTaskIdOrderByCreatedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getCommentsByTaskScroll_ShouldReturnResponseWithHasMoreFalse_WhenLessThanLimit() {
        String cursor = null;
        int limit = 2;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<CommentJpa> content = List.of(mockComment);
        Slice<CommentJpa> mockSlice = new SliceImpl<>(content, pageable, false);
        List<CommentResponse> mockResponses = List.of(mockResponse);

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(mockSlice);
        when(commentMapper.toResponseList(content)).thenReturn(mockResponses);

        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);

        assertFalse(result.hasMore());
        assertEquals(fixedCreatedAt.toString(), result.nextCursor());
        verify(taskRepository).existsById(1L);
        verify(commentMapper).toResponseList(content);
    }

    @Test
    void getCommentsByTaskScroll_ShouldReturnHasMoreTrue_WhenBatchFull() {
        int limit = 2;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<CommentJpa> fullContent = List.of(mockComment, mockComment2);
        Slice<CommentJpa> mockSlice = new SliceImpl<>(fullContent, pageable, true);
        List<CommentResponse> mockResponses = List.of(mockResponse, mockResponse);

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(mockSlice);
        when(commentMapper.toResponseList(fullContent)).thenReturn(mockResponses);

        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, null, limit);

        assertTrue(result.hasMore());
        assertEquals(mockComment2.getCreatedAt().toString(), result.nextCursor());
        verify(taskRepository).existsById(1L);
    }

    @Test
    void getCommentsByTaskScroll_ShouldParseCursor_WhenNonBlank() {
        String cursor = "2025-11-12T10:00:00Z";
        int limit = 2;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<CommentJpa> content = List.of(mockComment);
        Slice<CommentJpa> mockSlice = new SliceImpl<>(content, pageable, false);
        List<CommentResponse> mockResponses = List.of(mockResponse);

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(mockSlice);
        when(commentMapper.toResponseList(content)).thenReturn(mockResponses);

        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);

        assertFalse(result.hasMore());
        assertEquals(fixedCreatedAt.toString(), result.nextCursor());
        verify(taskRepository).existsById(1L);
        verify(commentMapper).toResponseList(content);
    }

    @Test
    void getCommentsByTaskScroll_ShouldReturnNullCursor_WhenEmptySlice() {
        String cursor = null;
        int limit = 2;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Slice<CommentJpa> emptySlice = new SliceImpl<>(List.of(), pageable, false);

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(emptySlice);
        when(commentMapper.toResponseList(List.of())).thenReturn(List.of());

        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);

        assertFalse(result.hasMore());
        assertNull(result.nextCursor());
        assertTrue(result.comments().isEmpty());
        verify(taskRepository).existsById(1L);
    }

    @Test
    void getCommentsByTaskScroll_ShouldThrowTaskNotFoundException_WhenTaskNotExists() {
        String cursor = null;
        int limit = 2;

        when(taskRepository.existsById(999L)).thenReturn(false);

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> commentService.getCommentsByTaskScroll(999L, cursor, limit));
        assertEquals("Task not found: ID " + 999L, exception.getMessage());

        verify(commentRepository, never()).findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(anyLong(), any(OffsetDateTime.class), any(Pageable.class));
    }

    @Test
    void getCommentsByTaskScroll_ShouldHandleEmptyCursorString() {
        String cursor = "";
        int limit = 2;
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<CommentJpa> content = List.of(mockComment);
        Slice<CommentJpa> mockSlice = new SliceImpl<>(content, pageable, false);
        List<CommentResponse> mockResponses = List.of(mockResponse);

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
                .thenReturn(mockSlice);
        when(commentMapper.toResponseList(content)).thenReturn(mockResponses);

        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);

        assertFalse(result.hasMore());
        assertEquals(fixedCreatedAt.toString(), result.nextCursor());
        verify(taskRepository).existsById(1L);
    }

    @Test
    void updateComment_ShouldUpdateContent_WhenValid() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(mockComment)).thenReturn(mockComment);
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse _ = commentService.updateComment(1L, request);

        assertEquals("Updated content", mockComment.getContent());
        verify(commentRepository).save(mockComment);
    }

    @Test
    void updateComment_ShouldUpdateUpdatedAt_WhenContentChanged() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated");
        OffsetDateTime oldUpdatedAt = mockComment.getUpdatedAt();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentRepository.save(mockComment)).thenAnswer(_ -> {
            mockComment.setUpdatedAt(OffsetDateTime.now().plusSeconds(1));
            return mockComment;
        });
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse _ = commentService.updateComment(1L, request);

        assertNotEquals(oldUpdatedAt, mockComment.getUpdatedAt());
        verify(commentRepository).save(mockComment);
    }

    @Test
    void updateComment_ShouldNotChangeContent_WhenContentNull() {
        UpdateCommentRequest request = new UpdateCommentRequest(null);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse _ = commentService.updateComment(1L, request);

        assertEquals("Test content", mockComment.getContent());
        verify(commentRepository, never()).save(any(CommentJpa.class));
    }

    @Test
    void updateComment_ShouldNotSave_WhenContentUnchanged() {
        UpdateCommentRequest request = new UpdateCommentRequest("Test content");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse result = commentService.updateComment(1L, request);

        assertEquals("Test content", mockComment.getContent());
        verify(commentRepository, never()).save(any(CommentJpa.class));
        assertEquals(mockResponse, result);
    }

    @Test
    void updateComment_ShouldThrowNotFound_WhenCommentNotExists() {
        UpdateCommentRequest request = new UpdateCommentRequest("Update");
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.updateComment(999L, request));
        assertEquals("Comment not found: 999", exception.getMessage());
    }

    @Test
    void updateComment_ShouldNotUpdate_WhenContentBlank() {
        UpdateCommentRequest request = new UpdateCommentRequest("   ");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse _ = commentService.updateComment(1L, request);

        assertEquals("Test content", mockComment.getContent());
        verify(commentRepository, never()).save(any(CommentJpa.class));
    }

    @Test
    void updateComment_ShouldNotUpdate_WhenContentEmpty() {
        UpdateCommentRequest request = new UpdateCommentRequest("");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        CommentResponse _ = commentService.updateComment(1L, request);

        assertEquals("Test content", mockComment.getContent());
        verify(commentRepository, never()).save(any(CommentJpa.class));
    }

    @Test
    void deleteComment_ShouldDelete_WhenExists() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_ShouldThrowNotFound_WhenNotExists() {
        when(commentRepository.existsById(999L)).thenReturn(false);

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.deleteComment(999L));
        assertEquals("Comment not found: 999", exception.getMessage());
    }

    @Test
    void createComment_ShouldUseTaskIdAndUserIdInBuilder() {
        CreateCommentRequest request = new CreateCommentRequest(123L, "Test content");

        when(userServiceClient.userExists(456L)).thenReturn(true);
        when(taskRepository.existsById(123L)).thenReturn(true);

        when(commentRepository.save(argThat(comment ->
                comment.getTaskId().equals(123L) &&
                        comment.getUserId().equals(456L) &&
                        comment.getContent().equals("Test content")
        ))).thenReturn(mockComment);
        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);

        commentService.createComment(request, 456L);

        verify(commentRepository).save(any(CommentJpa.class));
    }
}