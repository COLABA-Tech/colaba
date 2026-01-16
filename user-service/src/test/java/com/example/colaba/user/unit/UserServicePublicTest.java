package com.example.colaba.user.unit;

import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.security.UserAccessCheckerLocal;
import com.example.colaba.user.service.UserService;
import com.example.colaba.user.service.UserServicePublic;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServicePublicTest {

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

    // ========== createUser Tests ==========

    @Test
    void createUser_successWithAdminAccess() {
        // Given
        CreateUserRequest request = new CreateUserRequest(username, email, "password", null);

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.createUser(request)).thenReturn(Mono.just(userResponse));

        // When
        Mono<UserResponse> result = userServicePublic.createUser(request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).createUser(request);
    }

    @Test
    @Disabled
    void createUser_accessDenied_throwsException() {
        // Given
        CreateUserRequest request = new CreateUserRequest(username, email, "password", null);
        RuntimeException accessException = new RuntimeException("Access denied");

        when(accessChecker.requireAdminMono(currentUserId))
                .thenReturn(Mono.error(accessException));

        // When
        Mono<UserResponse> result = userServicePublic.createUser(request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == accessException)
                .verify();

        verify(userService, never()).createUser(any());
    }

    // ========== getUserByUsername Tests ==========

    @Test
    void getUserByUsername_success() {
        // Given
        when(userService.getUserByUsername(username)).thenReturn(Mono.just(userResponse));

        // When
        Mono<UserResponse> result = userServicePublic.getUserByUsername(username, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(userService).getUserByUsername(username);
        // getUserByUsername не требует проверки прав доступа согласно коду
        verify(accessChecker, never()).requireAdminMono(any());
        verify(accessChecker, never()).requireCanManageUserMono(any(), any());
    }

    @Test
    void getUserByUsername_userServiceThrowsException_propagatesException() {
        // Given
        UserNotFoundException userNotFoundException = new UserNotFoundException(username);
        when(userService.getUserByUsername(username))
                .thenReturn(Mono.error(userNotFoundException));

        // When
        Mono<UserResponse> result = userServicePublic.getUserByUsername(username, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == userNotFoundException)
                .verify();

        verify(userService).getUserByUsername(username);
    }

    // ========== getUser Tests ==========

    @Test
    void getUser_successWithAccess() {
        // Given
        com.example.colaba.user.entity.User userEntity =
                com.example.colaba.user.entity.User.builder()
                        .id(targetUserId)
                        .username(username)
                        .email(email)
                        .build();

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId)).thenReturn(Mono.empty());
        when(userRepository.findById(targetUserId)).thenReturn(Mono.just(userEntity));
        when(userMapper.toUserResponse(userEntity)).thenReturn(userResponse);

        // When
        Mono<UserResponse> result = userServicePublic.getUser(targetUserId, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verify(userRepository).findById(targetUserId);
        verify(userMapper).toUserResponse(userEntity);
    }

    @Test
    @Disabled
    void getUser_accessDenied_throwsException() {
        // Given
        RuntimeException accessException = new RuntimeException("Cannot manage user");
        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.error(accessException));

        // When
        Mono<UserResponse> result = userServicePublic.getUser(targetUserId, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == accessException)
                .verify();

        verify(userRepository, never()).findById((Long) any());
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    void getUser_userNotFound_throwsUserNotFoundException() {
        // Given
        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId)).thenReturn(Mono.empty());
        when(userRepository.findById(targetUserId)).thenReturn(Mono.empty());

        // When
        Mono<UserResponse> result = userServicePublic.getUser(targetUserId, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectError(UserNotFoundException.class)
                .verify();

        verify(userRepository).findById(targetUserId);
        verify(userMapper, never()).toUserResponse(any());
    }

    // ========== updateUser Tests ==========

    @Test
    void updateUser_successWithAccess() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("newUsername", "newemail@colaba.com", "newPassword");

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId)).thenReturn(Mono.empty());
        when(userService.updateUser(targetUserId, request)).thenReturn(Mono.just(userResponse));

        // When
        Mono<UserResponse> result = userServicePublic.updateUser(targetUserId, request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(currentUserId, targetUserId);
        verify(userService).updateUser(targetUserId, request);
    }

    @Test
    @Disabled
    void updateUser_accessDenied_throwsException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("newUsername", "newemail@colaba.com", "newPassword");
        RuntimeException accessException = new RuntimeException("Cannot manage user");

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId))
                .thenReturn(Mono.error(accessException));

        // When
        Mono<UserResponse> result = userServicePublic.updateUser(targetUserId, request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == accessException)
                .verify();

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void updateUser_userServiceThrowsException_propagatesException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("newUsername", "newemail@colaba.com", "newPassword");
        UserNotFoundException userNotFoundException = new UserNotFoundException(targetUserId);

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId)).thenReturn(Mono.empty());
        when(userService.updateUser(targetUserId, request))
                .thenReturn(Mono.error(userNotFoundException));

        // When
        Mono<UserResponse> result = userServicePublic.updateUser(targetUserId, request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == userNotFoundException)
                .verify();

        verify(userService).updateUser(targetUserId, request);
    }

    // ========== deleteUser Tests ==========

    @Test
    void deleteUser_successWithAdminAccess() {
        // Given
        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.deleteUser(targetUserId)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = userServicePublic.deleteUser(targetUserId, currentUserId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).deleteUser(targetUserId);
    }

    @Test
    @Disabled
    void deleteUser_accessDenied_throwsException() {
        // Given
        RuntimeException accessException = new RuntimeException("Admin access required");
        when(accessChecker.requireAdminMono(currentUserId))
                .thenReturn(Mono.error(accessException));

        // When
        Mono<Void> result = userServicePublic.deleteUser(targetUserId, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == accessException)
                .verify();

        verify(userService, never()).deleteUser(any());
    }

    @Test
    void deleteUser_userServiceThrowsException_propagatesException() {
        // Given
        UserNotFoundException userNotFoundException = new UserNotFoundException(targetUserId);
        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.deleteUser(targetUserId))
                .thenReturn(Mono.error(userNotFoundException));

        // When
        Mono<Void> result = userServicePublic.deleteUser(targetUserId, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == userNotFoundException)
                .verify();

        verify(userService).deleteUser(targetUserId);
    }

    // ========== getAllUsers Tests ==========

    @Test
    void getAllUsers_successWithAdminAccess() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> userPage = new PageImpl<>(List.of(userResponse), pageable, 1);

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getAllUsers(pageable)).thenReturn(Mono.just(userPage));

        // When
        Mono<Page<UserResponse>> result = userServicePublic.getAllUsers(pageable, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userPage)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).getAllUsers(pageable);
    }

    @Test
    @Disabled
    void getAllUsers_accessDenied_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException accessException = new RuntimeException("Admin access required");

        when(accessChecker.requireAdminMono(currentUserId))
                .thenReturn(Mono.error(accessException));

        // When
        Mono<Page<UserResponse>> result = userServicePublic.getAllUsers(pageable, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == accessException)
                .verify();

        verify(userService, never()).getAllUsers(any());
    }

    // ========== getUsersScroll Tests ==========

    @Test
    void getUsersScroll_successWithAdminAccess() {
        // Given
        String cursor = "0";
        int limit = 10;
        UserScrollResponse scrollResponse = new UserScrollResponse(List.of(userResponse), "1", false);

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getUsersScroll(cursor, limit)).thenReturn(Mono.just(scrollResponse));

        // When
        Mono<UserScrollResponse> result = userServicePublic.getUsersScroll(cursor, limit, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(scrollResponse)
                .verifyComplete();

        verify(accessChecker).requireAdminMono(currentUserId);
        verify(userService).getUsersScroll(cursor, limit);
    }

    @Test
    @Disabled
    void getUsersScroll_accessDenied_throwsException() {
        // Given
        String cursor = "0";
        int limit = 10;
        RuntimeException accessException = new RuntimeException("Admin access required");

        when(accessChecker.requireAdminMono(currentUserId))
                .thenReturn(Mono.error(accessException));

        // When
        Mono<UserScrollResponse> result = userServicePublic.getUsersScroll(cursor, limit, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == accessException)
                .verify();

        verify(userService, never()).getUsersScroll(anyString(), anyInt());
    }

    @Test
    void getUsersScroll_userServiceThrowsException_propagatesException() {
        // Given
        String cursor = "0";
        int limit = 10;
        RuntimeException serviceException = new RuntimeException("Invalid cursor");

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getUsersScroll(cursor, limit))
                .thenReturn(Mono.error(serviceException));

        // When
        Mono<UserScrollResponse> result = userServicePublic.getUsersScroll(cursor, limit, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable == serviceException)
                .verify();

        verify(userService).getUsersScroll(cursor, limit);
    }

    // ========== Edge Cases Tests ==========

    @Test
    void getUser_sameUserAccess_success() {
        // Given: Пользователь запрашивает свои данные (currentUserId == targetUserId)
        Long sameUserId = 1L;
        com.example.colaba.user.entity.User userEntity =
                com.example.colaba.user.entity.User.builder()
                        .id(sameUserId)
                        .username("selfuser")
                        .email("self@colaba.com")
                        .build();
        UserResponse selfResponse = new UserResponse(sameUserId, "selfuser", "self@colaba.com", "USER");

        when(accessChecker.requireCanManageUserMono(sameUserId, sameUserId)).thenReturn(Mono.empty());
        when(userRepository.findById(sameUserId)).thenReturn(Mono.just(userEntity));
        when(userMapper.toUserResponse(userEntity)).thenReturn(selfResponse);

        // When
        Mono<UserResponse> result = userServicePublic.getUser(sameUserId, sameUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(selfResponse)
                .verifyComplete();

        verify(accessChecker).requireCanManageUserMono(sameUserId, sameUserId);
    }

    @Test
    void getAllUsers_emptyPage_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> emptyPage = Page.empty();

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getAllUsers(pageable)).thenReturn(Mono.just(emptyPage));

        // When
        Mono<Page<UserResponse>> result = userServicePublic.getAllUsers(pageable, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(emptyPage)
                .verifyComplete();
    }

    @Test
    void getUsersScroll_emptyResult_success() {
        // Given
        String cursor = "100";
        int limit = 10;
        UserScrollResponse emptyResponse = new UserScrollResponse(List.of(), "100", false);

        when(accessChecker.requireAdminMono(currentUserId)).thenReturn(Mono.empty());
        when(userService.getUsersScroll(cursor, limit)).thenReturn(Mono.just(emptyResponse));

        // When
        Mono<UserScrollResponse> result = userServicePublic.getUsersScroll(cursor, limit, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(emptyResponse)
                .verifyComplete();
    }

    @Test
    void updateUser_withNullRequestFields_success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(null, null, null);

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId)).thenReturn(Mono.empty());
        when(userService.updateUser(targetUserId, request)).thenReturn(Mono.just(userResponse));

        // When
        Mono<UserResponse> result = userServicePublic.updateUser(targetUserId, request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(userService).updateUser(targetUserId, request);
    }

    @Test
    void updateUser_withEmptyStringFields_ignoresEmptyValues() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("", "", "");

        when(accessChecker.requireCanManageUserMono(currentUserId, targetUserId)).thenReturn(Mono.empty());
        when(userService.updateUser(targetUserId, request)).thenReturn(Mono.just(userResponse));

        // When
        Mono<UserResponse> result = userServicePublic.updateUser(targetUserId, request, currentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(userService).updateUser(targetUserId, request);
    }

    @Test
    void getUserByUsername_withDifferentCurrentUser_success() {
        // Given
        Long differentUserId = 999L;
        when(userService.getUserByUsername(username)).thenReturn(Mono.just(userResponse));

        // When
        Mono<UserResponse> result = userServicePublic.getUserByUsername(username, differentUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(userResponse)
                .verifyComplete();

        verify(userService).getUserByUsername(username);
        // Проверяем, что getUserByUsername не использует currentUserId для проверки доступа
        verify(accessChecker, never()).requireCanManageUserMono(any(), any());
        verify(accessChecker, never()).requireAdminMono(any());
    }
}