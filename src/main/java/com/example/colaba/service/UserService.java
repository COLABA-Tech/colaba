package com.example.colaba.service;

import com.example.colaba.dto.CreateUserRequest;
import com.example.colaba.dto.UpdateUserRequest;
import com.example.colaba.dto.UserResponse;
import com.example.colaba.dto.UserScrollResponse;
import com.example.colaba.entity.User;
import com.example.colaba.exception.DuplicateEntityException;
import com.example.colaba.exception.UserNotFoundException;
import com.example.colaba.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateEntityException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEntityException("Email already exists");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getRole()
        );
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return convertToDto(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToDto);
    }

    public UserScrollResponse getUsersScroll(String cursor, int limit) {
        long offset = cursor.isEmpty() ? 0 : Long.parseLong(cursor);
        Pageable pageable = PageRequest.of(0, limit);  // Offset via custom query if needed
        Slice<User> users = userRepository.findAllByOffset(offset, limit);  // Custom repo method for Slice (no total)
        UserScrollResponse resp = new UserScrollResponse();
        resp.setUsers(users.getContent().stream().map(this::convertToDto).collect(Collectors.toList()));
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
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        User saved = userRepository.save(user);
        return convertToDto(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse convertToDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}