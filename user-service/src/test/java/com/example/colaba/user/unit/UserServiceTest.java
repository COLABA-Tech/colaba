package com.example.colaba.user.unit;

import com.example.colaba.shared.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.shared.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.user.circuit.ProjectServiceClientWrapper;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserResponse;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectServiceClientWrapper projectClientWrapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request;
    private User savedUser;

    private final Long test_id = 1L;
    private final String test_username = "test";
    private final String test_email = "test@colaba.com";

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest(test_username, test_email);
        savedUser = User.builder().id(test_id).username(test_username).email(test_email).build();
    }

    @Test
    void createUser_success() {
        // Given (arrange)
        when(userRepository.existsByUsername(test_username)).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(test_email)).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When (act)
        Mono<UserResponse> resultMono = userService.createUser(request);

        // Then (assert)
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(test_username) &&
                                result.email().equals(test_email))
                .verifyComplete();

        verify(userRepository).existsByUsername(test_username);
        verify(userRepository).existsByEmail(test_email);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        // Given
        when(userRepository.existsByUsername(test_username)).thenReturn(Mono.just(true));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));

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
        // Given (arrange)
        when(userRepository.findByUsername(test_username)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(userService.getUserByUsername(test_username))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: USERNAME " + test_username))
                .verify();

        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void updateUser_success() {
        // Given
        String newUsername = "newUsername";
        String newEmail = "newemail@colaba.com";
        UpdateUserRequest request = new UpdateUserRequest(newUsername, newEmail);

        User updatedUser = User.builder().id(test_id).username(newUsername).email(newEmail).build();

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.existsByUsernameAndIdNot(anyString(), anyLong())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        when(userMapper.toUserResponse(updatedUser)).thenReturn(new UserResponse(test_id, newUsername, newEmail));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        UpdateUserRequest request = new UpdateUserRequest(newUsername, null);

        User updatedUser = User.builder().id(test_id).username(newUsername).email(test_email).build();

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.existsByUsernameAndIdNot(newUsername, test_id)).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        when(userMapper.toUserResponse(updatedUser)).thenReturn(new UserResponse(test_id, newUsername, test_email));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mono<UserResponse> resultMono = userService.updateUser(test_id, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(newUsername) &&
                                result.email().equals(test_email))
                .verifyComplete();

        verify(userRepository).findById(test_id);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(updatedUser);
    }

    @Test
    void updateUser_partialUpdate_email_success() {
        // Given
        String newEmail = "newemail@colaba.com";
        UpdateUserRequest request = new UpdateUserRequest(null, newEmail);

        User updatedUser = User.builder().id(test_id).username(test_username).email(newEmail).build();

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userRepository.existsByEmailAndIdNot(newEmail, test_id)).thenReturn(Mono.just(false));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));
        when(userMapper.toUserResponse(updatedUser)).thenReturn(new UserResponse(test_id, test_username, newEmail));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        UpdateUserRequest request = new UpdateUserRequest(" ", " ");

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_noChanges_returnsUnchangedUser() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(test_username, test_email);

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mono<UserResponse> resultMono = userService.updateUser(test_id, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result ->
                        result.id().equals(test_id) &&
                                result.username().equals(test_username) &&
                                result.email().equals(test_email))
                .verifyComplete();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_notFound_throwsException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("newUsername", "newemail@colaba.com");
        when(userRepository.findById(test_id)).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        StepVerifier.create(userService.updateUser(test_id, request))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: ID " + test_id))
                .verify();

        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void deleteUser_success() {
        // Given
        UserJpa userJpa = UserJpa.builder().id(test_id).build();
        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userMapper.toUserJpa(savedUser)).thenReturn(userJpa);
        when(projectClientWrapper.findByOwner(userJpa)).thenReturn(List.of());
        when(userRepository.deleteById(test_id)).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mono<Void> resultMono = userService.deleteUser(test_id);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userRepository).deleteById(test_id);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        // Given
        when(userRepository.findById(test_id)).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When & Then
        StepVerifier.create(userService.deleteUser(test_id))
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("User not found: ID " + test_id))
                .verify();

        verify(userRepository, never()).deleteById(test_id);
    }

    @Test
    void deleteUser_success_withProjects() {
        // Given
        UserJpa userJpa = UserJpa.builder().id(test_id).build();
        Project project1 = Project.builder().id(1L).name("Project 1").owner(userJpa).build();
        Project project2 = Project.builder().id(2L).name("Project 2").owner(userJpa).build();
        List<Project> ownedProjects = List.of(project1, project2);

        when(userRepository.findById(test_id)).thenReturn(Mono.just(savedUser));
        when(userMapper.toUserJpa(savedUser)).thenReturn(userJpa);
        when(projectClientWrapper.findByOwner(userJpa)).thenReturn(ownedProjects);
        when(userRepository.deleteById(test_id)).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Mono<Void> resultMono = userService.deleteUser(test_id);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userRepository).findById(test_id);
        verify(userMapper).toUserJpa(savedUser);
        verify(projectClientWrapper).findByOwner(userJpa);
        verify(projectClientWrapper).deleteProject(1L);
        verify(projectClientWrapper).deleteProject(2L);
        verify(userRepository).deleteById(test_id);
    }

    @Test
    void getAllUsers_pagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(users));
        when(userMapper.toUserResponseList(anyList())).thenReturn(
                List.of(new UserResponse(test_id, test_username, test_email))
        );

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().get(0).id().equals(test_id))
                .verifyComplete();
    }

    @Test
    void getUsersScroll_withEmptyCursor_returnsFirstPage() {
        // Given
        String cursor = "";
        int limit = 10;
        List<User> users = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(users));
        when(userMapper.toUserResponseList(users)).thenReturn(
                List.of(new UserResponse(test_id, test_username, test_email))
        );

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

        User user1 = savedUser;
        User user2 = User.builder().id(2L).username("test2").email("test2@colaba.com").build();
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(users));
        when(userMapper.toUserResponseList(anyList())).thenAnswer(invocation -> {
            List<User> inputUsers = invocation.getArgument(0);
            return inputUsers.stream()
                    .map(user -> new UserResponse(user.getId(), user.getUsername(), user.getEmail()))
                    .toList();
        });

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
        List<User> allUsers = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().isEmpty() &&
                                page.getTotalElements() == 1 &&
                                page.getPageable().getOffset() == 100)
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper, never()).toUserResponseList(anyList());
    }

    @Test
    void getAllUsers_exactBoundaryCase_returnsLastPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);
        List<User> allUsers = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));
        when(userMapper.toUserResponseList(List.of(savedUser)))
                .thenReturn(List.of(new UserResponse(test_id, test_username, test_email)));

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getTotalElements() == 1 &&
                                !page.hasNext())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(List.of(savedUser));
    }

    @Test
    void getAllUsers_startOffsetEqualsListSize_returnsEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 1);
        List<User> allUsers = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));
        when(userMapper.toUserResponseList(List.of())).thenReturn(List.of());

        // When
        Mono<Page<UserResponse>> resultMono = userService.getAllUsers(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().isEmpty() &&
                                page.getTotalElements() == 1 &&
                                page.getPageable().getOffset() == 1)
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(List.of());
    }

    @Test
    void getUsersScroll_cursorGreaterThanListSize_returnsEmptyResponse() {
        // Given
        String cursor = "100";
        int limit = 10;
        List<User> allUsers = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("100") &&
                                !response.hasMore())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper, never()).toUserResponseList(anyList());
    }

    @Test
    void getUsersScroll_cursorEqualsListSize_returnsEmptyResponse() {
        // Given
        String cursor = "1";
        int limit = 10;
        List<User> allUsers = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));
        when(userMapper.toUserResponseList(List.of())).thenReturn(List.of());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(List.of());
    }

    @Test
    void getUsersScroll_cursorZeroWithEmptyList_returnsEmptyResponse() {
        // Given
        String cursor = "0";
        int limit = 10;
        List<User> allUsers = List.of();

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));
        when(userMapper.toUserResponseList(List.of())).thenReturn(List.of());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("0") &&
                                !response.hasMore())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(List.of());
    }

    @Test
    void getUsersScroll_emptyCursorWithEmptyList_returnsEmptyResponse() {
        // Given
        String cursor = "";
        int limit = 10;
        List<User> allUsers = List.of();

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));
        when(userMapper.toUserResponseList(List.of())).thenReturn(List.of());

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("0") &&
                                !response.hasMore())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(List.of());
    }

    @Test
    void getUsersScroll_exactBoundaryCursor_returnsLastElement() {
        // Given
        String cursor = "0";
        int limit = 1;
        List<User> allUsers = List.of(savedUser);

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));
        when(userMapper.toUserResponseList(List.of(savedUser)))
                .thenReturn(List.of(new UserResponse(test_id, test_username, test_email)));

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().size() == 1 &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper).toUserResponseList(List.of(savedUser));
    }

    @Test
    void getUsersScroll_cursorOneWithEmptyList_returnsEmptyResponse() {
        // Given
        String cursor = "1";
        int limit = 10;
        List<User> allUsers = List.of();

        when(userRepository.findAll()).thenReturn(Flux.fromIterable(allUsers));

        // When
        Mono<UserScrollResponse> resultMono = userService.getUsersScroll(cursor, limit);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.users().isEmpty() &&
                                response.nextCursor().equals("1") &&
                                !response.hasMore())
                .verifyComplete();

        verify(userRepository).findAll();
        verify(userMapper, never()).toUserResponseList(anyList());
    }
}
