package com.example.colaba.user.service;

import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.security.UserAccessCheckerLocal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServicePublic {
    private final UserService userService;
    private final UserAccessCheckerLocal accessChecker;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Mono<UserResponse> createUser(CreateUserRequest request, Long currentUserId) {
        return accessChecker.requireAdminMono(currentUserId)
                .then(userService.createUser(request));
    }

    public Mono<UserResponse> getUserByUsername(String username, Long currentUserId) {
        return userService.getUserByUsername(username);
    }

    public Mono<UserResponse> getUser(Long targetUserId, Long currentUserId) {
        return accessChecker.requireCanManageUserMono(currentUserId, targetUserId)
                .then(userRepository.findById(targetUserId)
                        .switchIfEmpty(Mono.error(new UserNotFoundException(targetUserId)))
                        .map(userMapper::toUserResponse));
    }

    public Mono<UserResponse> updateUser(Long targetUserId, UpdateUserRequest request, Long currentUserId) {
        return accessChecker.requireCanManageUserMono(currentUserId, targetUserId)
                .then(userService.updateUser(targetUserId, request));
    }

    public Mono<Void> deleteUser(Long targetUserId, Long currentUserId) {
        return accessChecker.requireAdminMono(currentUserId)
                .then(userService.deleteUser(targetUserId));
    }

    public Mono<Page<UserResponse>> getAllUsers(Pageable pageable, Long currentUserId) {
        return accessChecker.requireAdminMono(currentUserId)
                .then(userService.getAllUsers(pageable));
    }

    public Mono<UserScrollResponse> getUsersScroll(String cursor, int limit, Long currentUserId) {
        return accessChecker.requireAdminMono(currentUserId)
                .then(userService.getUsersScroll(cursor, limit));
    }
}