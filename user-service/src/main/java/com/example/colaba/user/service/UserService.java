package com.example.colaba.user.service;

import com.example.colaba.shared.client.ProjectServiceClient;
import com.example.colaba.shared.dto.user.CreateUserRequest;
import com.example.colaba.shared.dto.user.UpdateUserRequest;
import com.example.colaba.shared.dto.user.UserResponse;
import com.example.colaba.shared.dto.user.UserScrollResponse;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.User;
import com.example.colaba.shared.entity.UserJpa;
import com.example.colaba.shared.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.shared.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ProjectServiceClient projectServiceClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TransactionalOperator transactionalOperator;

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return userRepository.existsByUsername(request.username())
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new DuplicateUserEntityUsernameException(request.username()));
                    }
                    return userRepository.existsByEmail(request.email());
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new DuplicateUserEntityEmailException(request.email()));
                    }

                    User user = User.builder()
                            .username(request.username())
                            .email(request.email())
                            .build();

                    return userRepository.save(user)
                            .map(userMapper::toUserResponse);
                })
                .as(transactionalOperator::transactional);
    }

    public Mono<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .map(userMapper::toUserResponse);
    }

    public Mono<User> getUserEntityById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }

    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .map(userMapper::toUserResponse);
    }

    public Mono<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .flatMap(user -> {
                    boolean hasChanges = false;

                    if (request.username() != null && !request.username().isBlank() && !request.username().equals(user.getUsername())) {
                        user.setUsername(request.username());
                        hasChanges = true;
                    }

                    if (request.email() != null && !request.email().isBlank() && !request.email().equals(user.getEmail())) {
                        user.setEmail(request.email());
                        hasChanges = true;
                    }

                    if (hasChanges) {
                        return userRepository.save(user);
                    } else {
                        return Mono.just(user);
                    }
                })
                .map(userMapper::toUserResponse)
                .as(transactionalOperator::transactional);
    }

    @Transactional
    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .flatMap(user -> {
                    UserJpa userJpa = userMapper.toUserJpa(user);
                    return Mono.fromCallable(() -> {
                                List<Project> ownedProjects = projectServiceClient.findByOwner(userJpa);

                                if (!ownedProjects.isEmpty()) {
                                    projectServiceClient.deleteAll();
                                }
                                return user;
                            })
                            .subscribeOn(Schedulers.boundedElastic())
                            .then(userRepository.deleteById(id));
                })
                .as(transactionalOperator::transactional);
    }

    public Mono<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return userRepository.findAll()
                .collectList()
                .map(allUsers -> {
                    // Ручная пагинация
                    int start = (int) pageable.getOffset();
                    int end = Math.min((start + pageable.getPageSize()), allUsers.size());

                    if (start > allUsers.size()) {
                        List<UserResponse> emptyContent = List.of();
                        return new PageImpl<>(emptyContent, pageable, allUsers.size());
                    }

                    List<User> pageContent = allUsers.subList(start, end);
                    List<UserResponse> content = userMapper.toUserResponseList(pageContent);
                    return (Page<UserResponse>) new PageImpl<>(content, pageable, allUsers.size());
                })
                .onErrorMap(Exception.class, e -> new IllegalArgumentException("Invalid pagination parameters: " + e.getMessage()));
    }

    public Mono<UserScrollResponse> getUsersScroll(String cursor, int limit) {
        long offset = cursor.isEmpty() ? 0 : Long.parseLong(cursor);

        return userRepository.findAll()
                .collectList()
                .map(allUsers -> {
                    // Ручная пагинация для scroll
                    int start = (int) offset;
                    int end = Math.min((start + limit), allUsers.size());

                    if (start > allUsers.size()) {
                        return new UserScrollResponse(List.of(), String.valueOf(offset), false);
                    }

                    List<User> pageContent = allUsers.subList(start, end);
                    List<UserResponse> userResponseList = userMapper.toUserResponseList(pageContent);
                    String nextCursor = String.valueOf(offset + userResponseList.size());
                    boolean hasMore = end < allUsers.size();
                    return new UserScrollResponse(userResponseList, nextCursor, hasMore);
                });
    }
}