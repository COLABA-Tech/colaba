package com.example.colaba.user.unit;

import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.shared.common.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.shared.common.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webflux.circuit.ProjectServiceClientWrapper;
import com.example.colaba.shared.webflux.circuit.TaskServiceClientWrapper;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectServiceClientWrapper projectServiceClient;

    @Mock
    private TaskServiceClientWrapper taskServiceClient;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private UserMapper userMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request;
    private User savedUser;

    private final Long test_id = 1L;
    private final String test_username = "test";
    private final String test_email = "test@colaba.com";

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest(
                test_username,
                test_email,
                "test123",
                UserRole.USER
        );

        savedUser = User.builder()
                .id(test_id)
                .username(test_username)
                .email(test_email)
                .role(UserRole.USER)
                .build();

        // Общие моки
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toUserResponse(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    String roleName = user.getRole() != null ? user.getRole().name() : "USER";
                    return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), roleName);
                });
    }

    @Test
    void createUser_success() {
        // Given
        when(userRepository.existsByUsername(test_username)).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(test_email)).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // When
        Mono<UserResponse> resultMono = userService.createUser(request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(test_username) &&
                                result.email().equals(test_email))
                .verifyComplete();

        verify(userRepository).existsByUsername(test_username);
        verify(userRepository).existsByEmail(test_email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        // Given
        when(userRepository.existsByUsername(test_username)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(userService.createUser(request))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateUserEntityUsernameException &&
                                throwable.getMessage().equals("Duplicate user entity: USERNAME " + test_username))
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        // Given
        when(userRepository.existsByUsername(test_username)).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(test_email)).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(userService.createUser(request))
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateUserEntityEmailException &&
                                throwable.getMessage().equals("Duplicate user entity: EMAIL " + test_email))
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUsername_success() {
        // Given
        when(userRepository.findByUsername(test_username)).thenReturn(Mono.just(savedUser));

        // When
        Mono<UserResponse> resultMono = userService.getUserByUsername(test_username);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(test_username) &&
                                result.email().equals(test_email))
                .verifyComplete();
    }

    @Test
    void getUserByUsername_notFound_throwsException() {
        // Given
        when(userRepository.findByUsername(test_username)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.getUserByUsername(test_username))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: USERNAME " + test_username))
                .verify();
    }

    @Test
    void getUserEntityById_success() {
        // Given
        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));

        // When
        Mono<User> resultMono = userService.getUserEntityById(test_id);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.getId().equals(test_id) &&
                                result.getUsername().equals(test_username) &&
                                result.getEmail().equals(test_email))
                .verifyComplete();
    }

    @Test
    void getUserEntityById_notFound_throwsException() {
        // Given
        when(userRepository.findById(test_id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.getUserEntityById(test_id))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: ID " + test_id))
                .verify();
    }

    @Test
    void updateUser_success() {
        // Given
        String newUsername = "newUsername";
        String newEmail = "newemail@colaba.com";
        UpdateUserRequest request = new UpdateUserRequest(newUsername, newEmail, null);

        User updatedUser = User.builder()
                .id(test_id)
                .username(newUsername)
                .email(newEmail)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.existsByUsernameAndIdNot(anyString(), anyLong())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        // When
        Mono<UserResponse> resultMono = userService.updateUser(test_id, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(newUsername) &&
                                result.email().equals(newEmail))
                .verifyComplete();
    }

    @Test
    void updateUser_partialUpdate_username_success() {
        // Given
        String newUsername = "newUsername";
        UpdateUserRequest request = new UpdateUserRequest(newUsername, null, null);

        User updatedUser = User.builder()
                .id(test_id)
                .username(newUsername)
                .email(test_email)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.existsByUsernameAndIdNot(newUsername, test_id)).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        // When
        Mono<UserResponse> resultMono = userService.updateUser(test_id, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(newUsername) &&
                                result.email().equals(test_email))
                .verifyComplete();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_partialUpdate_email_success() {
        // Given
        String newEmail = "newemail@colaba.com";
        UpdateUserRequest request = new UpdateUserRequest(null, newEmail, null);

        User updatedUser = User.builder()
                .id(test_id)
                .username(test_username)
                .email(newEmail)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.existsByEmailAndIdNot(newEmail, test_id)).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        // When
        Mono<UserResponse> resultMono = userService.updateUser(test_id, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(test_username) &&
                                result.email().equals(newEmail))
                .verifyComplete();
    }

    @Test
    void updateUser_blankFields_ignoresBlankValues() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(" ", " ", null);

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // When
        Mono<UserResponse> resultMono = userService.updateUser(test_id, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(test_username) &&
                                result.email().equals(test_email))
                .verifyComplete();

        verify(userRepository, never()).existsByUsernameAndIdNot(anyString(), anyLong());
        verify(userRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_notFound_throwsException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("newUserName", "newemail@colaba.com", "p34hncso");

        when(userRepository.findById(test_id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.updateUser(test_id, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: ID " + test_id))
                .verify();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_success() {
        // Given
        when(projectServiceClient.handleUserDeletion(test_id)).thenReturn(Mono.empty());
        when(taskServiceClient.handleUserDeletion(test_id)).thenReturn(Mono.empty());
        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.deleteById(test_id)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = userService.deleteUser(test_id);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(projectServiceClient).handleUserDeletion(test_id);
        verify(taskServiceClient).handleUserDeletion(test_id);
        verify(userRepository).deleteById(test_id);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        // Given
        when(projectServiceClient.handleUserDeletion(test_id)).thenReturn(Mono.empty());
        when(taskServiceClient.handleUserDeletion(test_id)).thenReturn(Mono.empty());
        when(userRepository.findById(test_id)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.deleteUser(test_id))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: ID " + test_id))
                .verify();

        verify(userRepository, never()).deleteById(test_id);
        verify(projectServiceClient).handleUserDeletion(test_id);
        verify(taskServiceClient).handleUserDeletion(test_id);
    }

    @Test
    void deleteUser_success_withProjects() {
        // Given
        ProjectResponse project1 = new ProjectResponse(1L, "Project 1", null, null);
        ProjectResponse project2 = new ProjectResponse(2L, "Project 2", null, null);

        when(projectServiceClient.handleUserDeletion(test_id)).thenReturn(Mono.empty());
        when(taskServiceClient.handleUserDeletion(test_id)).thenReturn(Mono.empty());
        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.deleteById(test_id)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = userService.deleteUser(test_id);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userRepository).findById(test_id);
        verify(projectServiceClient).handleUserDeletion(test_id);
        verify(taskServiceClient).handleUserDeletion(test_id);
        verify(userRepository).deleteById(test_id);
    }

    @Test
    void getAllUsers_pagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.just(savedUser));
        when(userRepository.count()).thenReturn(Mono.just(1L));

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().id().equals(test_id))
                .verifyComplete();
    }

    @Test
    void getUsersScroll_withEmptyCursor_returnsFirstPage() {
        // Given
        String cursor = "";
        int limit = 10;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.just(savedUser));

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().size() == 1 &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getUsersScroll_withCursor_returnsNextPage() {
        // Given
        String cursor = "1";
        int limit = 10;
        String expectedNextCursor = "2";

        User user2 = User.builder()
                .id(2L)
                .username("test2")
                .email("test2@colaba.com")
                .role(UserRole.USER)
                .build();

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.just(user2));

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().size() == 1 &&
                                response.nextCursor().equals(expectedNextCursor) &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getAllUsers_startOffsetGreaterThanListSize_returnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(10, 10);

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());
        when(userRepository.count()).thenReturn(Mono.just(1L));

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().isEmpty() &&
                                page.getTotalElements() == 1 &&
                                page.getPageable().getOffset() == 100)
                .verifyComplete();
    }

    @Test
    void getAllUsers_exactBoundaryCase_returnsLastPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.just(savedUser));
        when(userRepository.count()).thenReturn(Mono.just(1L));

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getTotalElements() == 1 &&
                                !page.hasNext())
                .verifyComplete();
    }

    @Test
    void getAllUsers_startOffsetEqualsListSize_returnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 1);

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());
        when(userRepository.count()).thenReturn(Mono.just(1L));

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().isEmpty() &&
                                page.getTotalElements() == 1 &&
                                page.getPageable().getOffset() == 1)
                .verifyComplete();
    }

    @Test
    void getUsersScroll_cursorGreaterThanListSize_returnsEmptyResponse() {
        // Given
        String cursor = "100";
        int limit = 10;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("100") &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getUsersScroll_cursorEqualsListSize_returnsEmptyResponse() {
        // Given
        String cursor = "1";
        int limit = 10;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getUsersScroll_cursorZeroWithEmptyList_returnsEmptyResponse() {
        // Given
        String cursor = "0";
        int limit = 10;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("0") &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getUsersScroll_emptyCursorWithEmptyList_returnsEmptyResponse() {
        // Given
        String cursor = "";
        int limit = 10;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().isEmpty() &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getUsersScroll_exactBoundaryCursor_returnsLastElement() {
        // Given
        String cursor = "0";
        int limit = 1;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.just(savedUser));

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().size() == 1 &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();
    }

    @Test
    void getUsersScroll_cursorOneWithEmptyList_returnsEmptyResponse() {
        // Given
        String cursor = "1";
        int limit = 10;

        when(r2dbcEntityTemplate
                .select(User.class)
                .from("users")
                .matching(any(Query.class))
                .all()).thenReturn(Flux.empty());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();
    }
}