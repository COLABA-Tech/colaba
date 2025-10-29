package com.example.colaba.service;

import com.example.colaba.dto.user.CreateUserRequest;
import com.example.colaba.dto.user.UpdateUserRequest;
import com.example.colaba.dto.user.UserResponse;
import com.example.colaba.dto.user.UserScrollResponse;
import com.example.colaba.entity.User;
import com.example.colaba.exception.common.DuplicateEntityException;
import com.example.colaba.exception.user.DuplicateUserEntityException;
import com.example.colaba.exception.user.UserNotFoundException;
import com.example.colaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserEntityException("Username " + request.getUsername() + " already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserEntityException("Email " + request.getEmail() + " already exists");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail()
        );
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return convertToResponse(user);
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
        return convertToResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToResponse);
    }

    public UserScrollResponse getUsersScroll(String cursor, int limit) {
        long offset = cursor.isEmpty() ? 0 : Long.parseLong(cursor);
        Slice<User> users = userRepository.findAllByOffset(offset, limit);  // Custom repo method for Slice (no total)
        UserScrollResponse resp = new UserScrollResponse();
        resp.setUsers(users.getContent().stream().map(this::convertToResponse).collect(Collectors.toList()));
        resp.setNextCursor(String.valueOf(offset + users.getNumberOfElements()));
        resp.setHasMore(!users.isLast());
        return resp;
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        User saved = userRepository.save(user);
        return convertToResponse(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}