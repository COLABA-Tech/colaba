package com.example.colaba.user.service;

import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.shared.common.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.common.exception.user.UserPasswordSameAsOldException;
import com.example.colaba.shared.webflux.circuit.ProjectServiceClientWrapper;
import com.example.colaba.shared.webflux.circuit.TaskServiceClientWrapper;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final ProjectServiceClientWrapper projectServiceClient;
    private final TaskServiceClientWrapper taskServiceClient;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TransactionalOperator transactionalOperator;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return transactionalOperator.transactional(
                Mono.just(request)
                        .flatMap(req -> userRepository.existsByUsername(req.username())
                                .flatMap(usernameExists -> {
                                    if (usernameExists) {
                                        return Mono.error(new DuplicateUserEntityUsernameException(req.username()));
                                    }
                                    return userRepository.existsByEmail(req.email())
                                            .flatMap(emailExists -> {
                                                if (emailExists) {
                                                    return Mono.error(new DuplicateUserEntityEmailException(req.email()));
                                                }
                                                User user = User.builder()
                                                        .username(req.username())
                                                        .email(req.email())
                                                        .password(request.password())
                                                        .role(req.role())
                                                        .build();

                                                return userRepository.save(user);
                                            });
                                })
                        )
                        .map(userMapper::toUserResponse)
        );
    }

    public Mono<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .map(userMapper::toUserResponse);
    }

    public Mono<User> getUserEntityById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }

    public Mono<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        return transactionalOperator.transactional(
                userRepository.findById(id)
                        .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                        .flatMap(existingUser -> applyUsernameUpdate(existingUser, request.username(), id))
                        .flatMap(updatedUser -> applyEmailUpdate(updatedUser, request.email(), id))
                        .flatMap(updatedUser -> applyPasswordUpdate(updatedUser, request.password()))
                        .flatMap(userRepository::save)
                        .map(userMapper::toUserResponse)
        );
    }

    private Mono<User> applyUsernameUpdate(User user, String newUsername, Long id) {
        if (newUsername == null || newUsername.isBlank() || newUsername.equals(user.getUsername())) {
            return Mono.just(user);
        }
        return userRepository.existsByUsernameAndIdNot(newUsername, id)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateUserEntityUsernameException(newUsername));
                    }
                    user.setUsername(newUsername);
                    return Mono.just(user);
                });
    }

    private Mono<User> applyEmailUpdate(User user, String newEmail, Long id) {
        if (newEmail == null || newEmail.isBlank() || newEmail.equals(user.getEmail())) {
            return Mono.just(user);
        }
        return userRepository.existsByEmailAndIdNot(newEmail, id)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateUserEntityEmailException(newEmail));
                    }
                    user.setEmail(newEmail);
                    return Mono.just(user);
                });
    }

    private Mono<User> applyPasswordUpdate(User user, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return Mono.just(user);
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return Mono.error(new UserPasswordSameAsOldException(user.getUsername()));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return Mono.just(user);
    }

    public Mono<Void> deleteUser(Long id) {
        return projectServiceClient.handleUserDeletion(id)
                .then(taskServiceClient.handleUserDeletion(id))
                .then(transactionalOperator.transactional(
                        userRepository.findById(id)
                                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                                .flatMap(_ -> userRepository.deleteById(id))
                ))
                .onErrorResume(Mono::error);
    }

    public Mono<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return r2dbcEntityTemplate.select(User.class)
                .from("users")
                .matching(Query.empty()
                        .with(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                        .sort(pageable.getSort()))
                .all()
                .map(userMapper::toUserResponse)
                .collectList()
                .zipWith(userRepository.count())
                .map(tuple -> new PageImpl<>(
                        tuple.getT1(),
                        pageable,
                        tuple.getT2()
                ));
    }

    public Mono<UserScrollResponse> getUsersScroll(String cursor, int limit) {
        long offset = cursor == null || cursor.isEmpty() || cursor.equals("0") ? 0 : Long.parseLong(cursor);

        Criteria criteria = offset > 0
                ? Criteria.where("id").greaterThan(offset)
                : Criteria.empty();

        return r2dbcEntityTemplate.select(User.class)
                .from("users")
                .matching(Query.query(criteria)
                        .limit(limit + 1)
                        .sort(org.springframework.data.domain.Sort.by("id").ascending()))
                .all()
                .map(userMapper::toUserResponse)
                .collectList()
                .map(users -> {
                    boolean hasMore = users.size() > limit;
                    List<UserResponse> result = hasMore
                            ? users.subList(0, limit)
                            : users;

                    String nextCursor = result.isEmpty()
                            ? cursor
                            : String.valueOf(result.getLast().id());

                    return new UserScrollResponse(result, nextCursor, hasMore);
                });
    }
}