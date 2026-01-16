package com.example.colaba.user.unit;

import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.security.UserAccessCheckerLocal;
import com.example.colaba.user.service.UserService;
import com.example.colaba.user.service.UserServicePublic;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServicePublicTest {

    @Mock
    private UserService userService;

    @Mock
    private UserAccessCheckerLocal accessChecker;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServicePublic userServicePublic;

    private final Long currentUserId = 1L;
    private final Long targetUserId = 2L;
    private final String username = "testuser";
    private final String email = "test@colaba.com";
    private final UserResponse userResponse = new UserResponse(targetUserId, username, email, "USER");

    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateUserRequest(username, email, "password", null);
        updateRequest = new UpdateUserRequest(username, email, null);
    }

    // ========== createUser ==========

    @Test
    void createUser_successWithAdminAccess() {
        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.createUser(createRequest)).thenReturn(Mono.just(userResponse));

        StepVerifier.create(userServicePublic.createUser(createRequest, currentUserId))
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).createUser(createRequest);
    }

    @Test
    void createUser_accessDenied_throwsException() {
        AccessDeniedException exception = new AccessDeniedException("Required user role: ADMIN");
        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.error(exception));

        StepVerifier.create(userServicePublic.createUser(createRequest, currentUserId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(accessChecker).requireAdminMono(currentUserId);
        verifyNoInteractions(userService);
    }

    // ========== getUserByUsername ==========

    @Test
    void getUserByUsername_success() {
        when(userService.getUserByUsername(username)).thenReturn(Mono.just(userResponse));

        StepVerifier.create(userServicePublic.getUserByUsername(username, currentUserId))
                .expectNext(userResponse)
                .verifyComplete();

        verify(userService).getUserByUsername(username);
    }

    // ========== getUser ==========

    @Test
    void getUser_successWithSelfAccess() {
        User userEntity = User.builder()
                .id(currentUserId)
                .username(username)
                .email(email)
                .role(UserRole.USER)
                .build();

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.empty());
        when(userService.getUserEntityById(targetUserId)).thenReturn(Mono.just(userEntity));
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        StepVerifier.create(userServicePublic.getUser(targetUserId, currentUserId))
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verify(userService).getUserEntityById(targetUserId);
        verify(userMapper).toUserResponse(userEntity);
    }

    @Test
    void getUser_successWithAdminAccess() {
        User userEntity = User.builder()
                .id(targetUserId)
                .username(username)
                .email(email)
                .role(UserRole.USER)
                .build();

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.empty());
        when(userService.getUserEntityById(targetUserId)).thenReturn(Mono.just(userEntity));
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        StepVerifier.create(userServicePublic.getUser(targetUserId, currentUserId))
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verify(userService).getUserEntityById(targetUserId);
        verify(userMapper).toUserResponse(userEntity);
    }

    @Test
    void getUser_accessDenied_throwsException() {
        AccessDeniedException exception = new AccessDeniedException("You can only manage your own account or as ADMIN");
        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(userServicePublic.getUser(targetUserId, currentUserId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verifyNoInteractions(userService);
        verifyNoInteractions(userMapper);
    }

    // ========== updateUser ==========

    @Test
    void updateUser_successWithSelfAccess() {
        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.empty());
        when(userService.updateUser(targetUserId, updateRequest)).thenReturn(Mono.just(userResponse));

        StepVerifier.create(userServicePublic.updateUser(targetUserId, updateRequest, currentUserId))
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verify(userService).updateUser(targetUserId, updateRequest);
    }

    @Test
    void updateUser_successWithAdminAccess() {
        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.empty());
        when(userService.updateUser(targetUserId, updateRequest)).thenReturn(Mono.just(userResponse));

        StepVerifier.create(userServicePublic.updateUser(targetUserId, updateRequest, currentUserId))
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verify(userService).updateUser(targetUserId, updateRequest);
    }

    @Test
    void updateUser_accessDenied_throwsException() {
        AccessDeniedException exception = new AccessDeniedException("You can only manage your own account or as ADMIN");
        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(userServicePublic.updateUser(targetUserId, updateRequest, currentUserId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verifyNoInteractions(userService);
    }

    // ========== deleteUser ==========

    @Test
    void deleteUser_successWithAdminAccess() {
        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.deleteUser(targetUserId)).thenReturn(Mono.empty());

        StepVerifier.create(userServicePublic.deleteUser(targetUserId, currentUserId))
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).deleteUser(targetUserId);
    }

    @Test
    void deleteUser_accessDenied_throwsException() {
        AccessDeniedException exception = new AccessDeniedException("Required user role: ADMIN");
        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.error(exception));

        StepVerifier.create(userServicePublic.deleteUser(targetUserId, currentUserId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(accessChecker).requireAdminMono(currentUserId);
        verifyNoInteractions(userService);
    }

    // ========== getAllUsers ==========

    @Test
    void getAllUsers_successWithAdminAccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> page = new PageImpl<>(List.of(userResponse));

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getAllUsers(pageable)).thenReturn(Mono.just(page));

        StepVerifier.create(userServicePublic.getAllUsers(pageable, currentUserId))
                .expectNext(page)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).getAllUsers(pageable);
    }

    @Test
    void getAllUsers_accessDenied_throwsException() {
        AccessDeniedException exception = new AccessDeniedException("Required user role: ADMIN");
        Pageable pageable = PageRequest.of(0, 10);

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.error(exception));

        StepVerifier.create(userServicePublic.getAllUsers(pageable, currentUserId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(accessChecker).requireAdminMono(currentUserId);
        verifyNoInteractions(userService);
    }

    // ========== getUsersScroll ==========

    @Test
    void getUsersScroll_successWithAdminAccess() {
        UserScrollResponse scrollResponse = new UserScrollResponse(List.of(userResponse), "10", true);

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getUsersScroll("5", 10)).thenReturn(Mono.just(scrollResponse));

        StepVerifier.create(userServicePublic.getUsersScroll("5", 10, currentUserId))
                .expectNext(scrollResponse)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).getUsersScroll("5", 10);
    }

    @Test
    void getUsersScroll_accessDenied_throwsException() {
        AccessDeniedException exception = new AccessDeniedException("Required user role: ADMIN");

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.error(exception));

        StepVerifier.create(userServicePublic.getUsersScroll("5", 10, currentUserId))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(accessChecker).requireAdminMono(currentUserId);
        verifyNoInteractions(userService);
    }
}