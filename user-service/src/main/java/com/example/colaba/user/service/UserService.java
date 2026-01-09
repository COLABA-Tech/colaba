package com.example.colaba.user.service;

import com.example.colaba.shared.dto.project.ProjectResponse;
import com.example.colaba.shared.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.shared.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.user.client.ProjectServiceClient;
import com.example.colaba.user.client.TaskServiceClient;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserResponse;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ProjectServiceClient projectServiceClient;
    private final TaskServiceClient taskServiceClient;
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

    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .map(userMapper::toUserResponse);
    }

    public Mono<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .flatMap(existingUser -> {

                    if (request.username() != null && !request.username().isBlank()
                            && !request.username().equals(existingUser.getUsername())) {
                        return userRepository.existsByUsernameAndIdNot(request.username(), id)
                                .flatMap(usernameExists -> {
                                    if (usernameExists) {
                                        return Mono.error(new DuplicateUserEntityUsernameException(request.username()));
                                    }
                                    existingUser.setUsername(request.username());

                                    if (request.email() != null && !request.email().isBlank()
                                            && !request.email().equals(existingUser.getEmail())) {
                                        return userRepository.existsByEmailAndIdNot(request.email(), id)
                                                .flatMap(emailExists -> {
                                                    if (emailExists) {
                                                        return Mono.error(new DuplicateUserEntityEmailException(request.email()));
                                                    }
                                                    existingUser.setEmail(request.email());
                                                    return userRepository.save(existingUser);
                                                });
                                    } else {
                                        return userRepository.save(existingUser);
                                    }
                                });
                    }

                    if (request.email() != null && !request.email().isBlank()
                            && !request.email().equals(existingUser.getEmail())) {
                        return userRepository.existsByEmailAndIdNot(request.email(), id)
                                .flatMap(emailExists -> {
                                    if (emailExists) {
                                        return Mono.error(new DuplicateUserEntityEmailException(request.email()));
                                    }
                                    existingUser.setEmail(request.email());
                                    return userRepository.save(existingUser);
                                });
                    }
                    return Mono.just(existingUser);
                })
                .map(userMapper::toUserResponse)
                .as(transactionalOperator::transactional);
    }

    public Mono<Void> deleteUser(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .flatMap(user -> Mono.fromCallable(() -> {
                            List<ProjectResponse> ownedProjects = projectServiceClient.findByOwnerId(user.getId());
                            if (ownedProjects == null) {
                                throw new UserNotFoundException(id);
                            }
                            if (!ownedProjects.isEmpty()) {
                                ownedProjects.forEach(project -> {
                                    projectServiceClient.deleteProject(project.id());
                                });
                            }
                            projectServiceClient.handleUserDeletion(id);
                            taskServiceClient.handleUserDeletion(id);
                            return user;
                        })
                        .subscribeOn(Schedulers.boundedElastic())
                        .then(userRepository.deleteById(id)))
                .as(transactionalOperator::transactional);
    }

    public Mono<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return userRepository.findAll()
                .collectList()
                .map(allUsers -> {
                    int start = (int) pageable.getOffset();
                    int end = Math.min((start + pageable.getPageSize()), allUsers.size());

                    if (start > allUsers.size()) {
                        List<UserResponse> emptyContent = List.of();
                        return new PageImpl<>(emptyContent, pageable, allUsers.size());
                    }

                    List<User> pageContent = allUsers.subList(start, end);
                    List<UserResponse> content = userMapper.toUserResponseList(pageContent);
                    return new PageImpl<>(content, pageable, allUsers.size());
                });
    }

    public Mono<UserScrollResponse> getUsersScroll(String cursor, int limit) {
        long offset = cursor.isEmpty() ? 0 : Long.parseLong(cursor);

        return userRepository.findAll()
                .collectList()
                .map(allUsers -> {
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