package com.example.colaba.service;

import com.example.colaba.dto.user.CreateUserRequest;
import com.example.colaba.dto.user.UpdateUserRequest;
import com.example.colaba.dto.user.UserResponse;
import com.example.colaba.dto.user.UserScrollResponse;
import com.example.colaba.entity.User;
import com.example.colaba.exception.user.DuplicateUserEntityException;
import com.example.colaba.exception.user.UserNotFoundException;
import com.example.colaba.mapper.user.UserMapper;
import com.example.colaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserEntityException("Username " + request.username() + " already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserEntityException("Email " + request.email() + " already exists");
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .build();
        User savedUser = userRepository.save(user);
        return UserMapper.INSTANCE.toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.INSTANCE.toUserResponseList(users);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return UserMapper.INSTANCE.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return UserMapper.INSTANCE.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return UserMapper.INSTANCE.toUserResponsePage(users);
    }

    @Transactional(readOnly = true)
    public UserScrollResponse getUsersScroll(String cursor, int limit) {
        long offset = cursor.isEmpty() ? 0 : Long.parseLong(cursor);
        Slice<User> users = userRepository.findAllByOffset(offset, limit);
        List<UserResponse> userResponseList = UserMapper.INSTANCE.toUserResponseList(users.getContent());
        String nextCursor = String.valueOf(offset + users.getNumberOfElements());
        boolean hasMore = !users.isLast();
        return new UserScrollResponse(userResponseList, nextCursor, hasMore);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        if (request.username() != null && !request.username().isBlank()) {
            user.setUsername(request.username());
        }
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }
        User saved = userRepository.save(user);
        return UserMapper.INSTANCE.toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}