package com.example.colaba.user.service;

import com.example.colaba.shared.dto.user.CreateUserRequest;
import com.example.colaba.shared.dto.user.UpdateUserRequest;
import com.example.colaba.shared.dto.user.UserResponse;
import com.example.colaba.shared.dto.user.UserScrollResponse;
import com.example.colaba.shared.entity.User;
import com.example.colaba.shared.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.shared.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.user.mapper.UserMapper;
import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    //    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserEntityUsernameException(request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserEntityEmailException(request.email());
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .build();
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toUserResponse(user);
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        boolean hasChanges = false;
        if (request.username() != null && !request.username().isBlank() && !request.username().equals(user.getUsername())) {
            user.setUsername(request.username());
            hasChanges = true;
        }
        if (request.email() != null && !request.email().isBlank() && !request.email().equals(user.getEmail())) {
            user.setEmail(request.email());
            hasChanges = true;
        }
        User saved = hasChanges ? userRepository.save(user) : user;
        return userMapper.toUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntityById(id);

        // TODO
//        List<Project> ownedProjects = projectRepository.findByOwner(user);
//        projectRepository.deleteAll(ownedProjects);

        userRepository.deleteById(id);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        try {
            Page<User> users = userRepository.findAll(pageable);
            return userMapper.toUserResponsePage(users);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pagination parameters: " + e.getMessage());
        }
    }

    public UserScrollResponse getUsersScroll(String cursor, int limit) {
        long offset = cursor.isEmpty() ? 0 : Long.parseLong(cursor);
        int page = (int) (offset / limit);
        Pageable pageable = PageRequest.of(page, limit, Sort.by("id"));
        Slice<User> users = userRepository.findAll(pageable);
        List<UserResponse> userResponseList = userMapper.toUserResponseList(users.getContent());
        String nextCursor = String.valueOf(offset + users.getNumberOfElements());
        boolean hasMore = users.hasNext();
        return new UserScrollResponse(userResponseList, nextCursor, hasMore);
    }
}