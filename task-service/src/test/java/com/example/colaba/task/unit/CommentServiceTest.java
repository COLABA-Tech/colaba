//package com.example.colaba.task.unit;
//
//import com.example.colaba.shared.client.UserServiceClient;
//import com.example.colaba.shared.dto.comment.CommentResponse;
//import com.example.colaba.shared.dto.comment.CommentScrollResponse;
//import com.example.colaba.shared.dto.comment.CreateCommentRequest;
//import com.example.colaba.shared.dto.comment.UpdateCommentRequest;
//import com.example.colaba.shared.entity.Comment;
//import com.example.colaba.user.entity.User;
//import com.example.colaba.user.entity.UserJpa;
//import com.example.colaba.shared.entity.task.Task;
//import com.example.colaba.shared.exception.comment.CommentNotFoundException;
//import com.example.colaba.shared.exception.task.TaskNotFoundException;
//import com.example.colaba.shared.exception.user.UserNotFoundException;
//import com.example.colaba.task.mapper.CommentMapper;
//import com.example.colaba.user.mapper.UserMapper;
//import com.example.colaba.task.repository.CommentRepository;
//import com.example.colaba.task.repository.TaskRepository;
//import com.example.colaba.task.service.CommentService;
//import feign.FeignException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CommentServiceTest {
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @Mock
//    private TaskRepository taskRepository;
//
//    @Mock
//    private CommentMapper commentMapper;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @InjectMocks
//    private CommentService commentService;
//
//    private User mockUser;
//    private UserJpa mockUserJpa;
//    private Task mockTask;
//    private Comment mockComment;
//    private Comment mockComment2;
//    private CommentResponse mockResponse;
//    private OffsetDateTime fixedCreatedAt = OffsetDateTime.now();
//
//    @BeforeEach
//    void setUp() {
//        fixedCreatedAt = OffsetDateTime.now();
//
//        mockUser = User.builder()
//                .id(1L)
//                .username("testuser")
//                .build();
//
//        mockUserJpa = UserJpa.builder()
//                .id(1L)
//                .username("testuser")
//                .build();
//
//        mockTask = Task.builder()
//                .id(1L)
//                .title("Test Task")
//                .build();
//
//        mockComment = Comment.builder()
//                .id(1L)
//                .task(mockTask)
//                .user(mockUserJpa)
//                .content("Test content")
//                .createdAt(fixedCreatedAt)
//                .updatedAt(fixedCreatedAt)
//                .build();
//
//        mockComment2 = Comment.builder()
//                .id(2L)
//                .task(mockTask)
//                .user(mockUserJpa)
//                .content("Test2")
//                .createdAt(fixedCreatedAt.minusSeconds(1))
//                .build();
//
//        mockResponse = new CommentResponse(1L, 1L, 1L, "Test content", fixedCreatedAt, fixedCreatedAt);
//    }
//
//    @Test
//    void createComment_ShouldReturnResponse_WhenValidRequest() {
//        CreateCommentRequest request = new CreateCommentRequest(1L, 1L, "Test content");
//
//        when(userServiceClient.getUserEntityById(1L)).thenReturn(mockUser);
//        when(userMapper.toUserJpa(mockUser)).thenReturn(mockUserJpa);
//        when(taskRepository.findById(1L)).thenReturn(Optional.of(mockTask));
//        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
//        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);
//
//        CommentResponse result = commentService.createComment(request);
//
//        assertEquals(mockResponse, result);
//        verify(userServiceClient).getUserEntityById(1L);
//        verify(userMapper).toUserJpa(mockUser);
//        verify(taskRepository).findById(1L);
//        verify(commentRepository).save(any(Comment.class));
//        verify(commentMapper).toResponse(mockComment);
//    }
//
//    @Test
//    void createComment_ShouldThrowUserNotFoundException_WhenUserNotExists() {
//        CreateCommentRequest request = new CreateCommentRequest(1L, 999L, "Test");
//
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(userServiceClient.getUserEntityById(999L)).thenThrow(feignException);
//
//        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
//                () -> commentService.createComment(request));
//        assertEquals("User not found: ID " + 999L, exception.getMessage());
//
//        verify(userServiceClient).getUserEntityById(999L);
//        verify(taskRepository, never()).findById(anyLong());
//        verify(commentRepository, never()).save(any(Comment.class));
//    }
//
//    @Test
//    void createCommFent_ShouldThrowTaskNotFoundException_WhenTaskNotExists() {
//        CreateCommentRequest request = new CreateCommentRequest(999L, 1L, "Test");
//
//        when(userServiceClient.getUserEntityById(1L)).thenReturn(mockUser);
//        when(userMapper.toUserJpa(mockUser)).thenReturn(mockUserJpa);
//        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
//
//        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
//                () -> commentService.createComment(request));
//        assertEquals("Task not found: ID " + 999L, exception.getMessage());
//
//        verify(userServiceClient).getUserEntityById(1L);
//        verify(userMapper).toUserJpa(mockUser);
//        verify(taskRepository).findById(999L);
//        verify(commentRepository, never()).save(any(Comment.class));
//    }
//
//    @Test
//    void getCommentById_ShouldReturnResponse_WhenCommentExists() {
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
//        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);
//
//        CommentResponse result = commentService.getCommentById(1L);
//
//        assertEquals(mockResponse, result);
//    }
//
//    @Test
//    void getCommentById_ShouldThrowNotFound_WhenCommentNotExists() {
//        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
//
//        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
//                () -> commentService.getCommentById(999L));
//        assertEquals("Comment not found: 999", exception.getMessage());
//    }
//
//    @Test
//    void getCommentsByTask_ShouldReturnPage_WhenValidTaskId() {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Comment> mockPage = new PageImpl<>(List.of(mockComment));
//        Page<CommentResponse> mockRespPage = new PageImpl<>(List.of(mockResponse));
//        when(commentRepository.findByTaskIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(mockPage);
//        when(commentMapper.toResponsePage(mockPage)).thenReturn(mockRespPage);
//
//        Page<CommentResponse> result = commentService.getCommentsByTask(1L, pageable);
//
//        assertEquals(1, result.getTotalElements());
//    }
//
//    @Test
//    void getCommentsByTaskScroll_ShouldReturnResponseWithHasMoreFalse_WhenLessThanLimit() {
//        String cursor = null;
//        int limit = 2;
//        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
//        List<Comment> content = List.of(mockComment);
//        Slice<Comment> mockSlice = new SliceImpl<>(content, pageable, false);
//        List<CommentResponse> mockResponses = List.of(mockResponse);
//        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
//                .thenReturn(mockSlice);
//        when(commentMapper.toResponseList(content)).thenReturn(mockResponses);
//
//        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);
//
//        assertFalse(result.hasMore());
//        assertEquals(fixedCreatedAt.toString(), result.nextCursor());
//        verify(commentMapper).toResponseList(content);
//    }
//
//    @Test
//    void getCommentsByTaskScroll_ShouldReturnHasMoreTrue_WhenBatchFull() {
//        int limit = 2;
//        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
//        List<Comment> fullContent = List.of(mockComment, mockComment2);
//        Slice<Comment> mockSlice = new SliceImpl<>(fullContent, pageable, true);
//        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
//                .thenReturn(mockSlice);
//
//        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, null, limit);
//
//        assertTrue(result.hasMore());
//        assertEquals(mockComment2.getCreatedAt().toString(), result.nextCursor());
//    }
//
//    @Test
//    void getCommentsByTaskScroll_ShouldParseCursor_WhenNonBlank() {
//        String cursor = "2025-11-12T10:00:00Z";
//        int limit = 2;
//        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
//        List<Comment> content = List.of(mockComment);
//        Slice<Comment> mockSlice = new SliceImpl<>(content, pageable, false);
//        List<CommentResponse> mockResponses = List.of(mockResponse);
//        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
//                .thenReturn(mockSlice);
//        when(commentMapper.toResponseList(content)).thenReturn(mockResponses);
//
//        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);
//
//        assertFalse(result.hasMore());
//        assertEquals(fixedCreatedAt.toString(), result.nextCursor());
//        verify(commentMapper).toResponseList(content);
//    }
//
//    @Test
//    void getCommentsByTaskScroll_ShouldReturnNullCursor_WhenEmptySlice() {
//        String cursor = null;
//        int limit = 2;
//        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
//        Slice<Comment> emptySlice = new SliceImpl<>(List.of(), pageable, false);
//        when(commentRepository.findByTaskIdAndCreatedAtBeforeOrderByCreatedAtDesc(eq(1L), any(OffsetDateTime.class), eq(pageable)))
//                .thenReturn(emptySlice);
//        when(commentMapper.toResponseList(List.of())).thenReturn(List.of());
//
//        CommentScrollResponse result = commentService.getCommentsByTaskScroll(1L, cursor, limit);
//
//        assertFalse(result.hasMore());
//        assertNull(result.nextCursor());
//        assertTrue(result.comments().isEmpty());
//    }
//
//    @Test
//    void updateComment_ShouldUpdateContent_WhenValid() {
//        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
//        when(commentRepository.save(mockComment)).thenReturn(mockComment);
//        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);
//
//        CommentResponse result = commentService.updateComment(1L, request);
//
//        assertEquals("Updated content", mockComment.getContent());
//        verify(commentRepository).save(mockComment);
//    }
//
//    @Test
//    void updateComment_ShouldUpdateUpdatedAt_WhenContentChanged() {
//        UpdateCommentRequest request = new UpdateCommentRequest("Updated");
//        OffsetDateTime oldUpdatedAt = mockComment.getUpdatedAt();
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
//        when(commentRepository.save(mockComment)).thenAnswer(inv -> {
//            mockComment.setUpdatedAt(OffsetDateTime.now().plusSeconds(1));
//            return mockComment;
//        });
//        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);
//
//        CommentResponse result = commentService.updateComment(1L, request);
//
//        assertNotEquals(oldUpdatedAt, mockComment.getUpdatedAt());
//    }
//
//    @Test
//    void updateComment_ShouldNotChangeContent_WhenContentNull() {
//        UpdateCommentRequest request = new UpdateCommentRequest(null);
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
//
//        CommentResponse result = commentService.updateComment(1L, request);
//
//        assertEquals("Test content", mockComment.getContent());
//        verify(commentRepository, never()).save(any(Comment.class));
//    }
//
//    @Test
//    void updateComment_ShouldNotSave_WhenContentUnchanged() {
//        UpdateCommentRequest request = new UpdateCommentRequest("Test content");
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
//        when(commentMapper.toResponse(mockComment)).thenReturn(mockResponse);
//
//        CommentResponse result = commentService.updateComment(1L, request);
//
//        assertEquals("Test content", mockComment.getContent());
//        verify(commentRepository, never()).save(any(Comment.class));
//        assertEquals(mockResponse, result);
//    }
//
//    @Test
//    void updateComment_ShouldThrowNotFound_WhenCommentNotExists() {
//        UpdateCommentRequest request = new UpdateCommentRequest("Update");
//        when(commentRepository.findById(999L)).thenReturn(Optional.empty());
//
//        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
//                () -> commentService.updateComment(999L, request));
//        assertEquals("Comment not found: 999", exception.getMessage());
//    }
//
//    @Test
//    void updateComment_ShouldNotUpdate_WhenContentBlank() {
//        UpdateCommentRequest request = new UpdateCommentRequest("   ");
//        when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));
//
//        CommentResponse result = commentService.updateComment(1L, request);
//
//        assertEquals("Test content", mockComment.getContent());
//        verify(commentRepository, never()).save(any(Comment.class));
//    }
//
//    @Test
//    void deleteComment_ShouldDelete_WhenExists() {
//        when(commentRepository.existsById(1L)).thenReturn(true);
//
//        commentService.deleteComment(1L);
//
//        verify(commentRepository).deleteById(1L);
//    }
//
//    @Test
//    void deleteComment_ShouldThrowNotFound_WhenNotExists() {
//        when(commentRepository.existsById(999L)).thenReturn(false);
//
//        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
//                () -> commentService.deleteComment(999L));
//        assertEquals("Comment not found: 999", exception.getMessage());
//    }
//}
