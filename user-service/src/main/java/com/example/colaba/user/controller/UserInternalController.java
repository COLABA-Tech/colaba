package com.example.colaba.user.controller;

import com.example.colaba.shared.common.dto.user.UserAuthDto;
import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
@Tag(name = "Users Internal", description = "Internal Users API")
public class UserInternalController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/{id}/exists")
    public Mono<Boolean> userExists(@PathVariable Long id) {
        return userRepository.existsById(id);
    }

    @GetMapping("/auth-by-username/{username}")
    public Mono<UserAuthDto> findForAuthByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException(username)))
                .map(user -> new UserAuthDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole().getValue()
                ));
    }

    @GetMapping("/auth-by-email/{email}")
    public Mono<UserAuthDto> findForAuthByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException(email)))
                .map(user -> new UserAuthDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole().getValue()
                ));
    }

    @PostMapping("/create")
    public Mono<UserAuthDto> createUser(@RequestBody UserAuthDto userAuthDto) {
        return userService.createUser(new CreateUserRequest(
                        userAuthDto.username(),
                        userAuthDto.email(),
                        userAuthDto.password(),
                        UserRole.valueOf(userAuthDto.role())
                ))
                .map(userResponse -> new UserAuthDto(
                        userResponse.id(),
                        userResponse.username(),
                        userResponse.email(),
                        userAuthDto.password(),
                        userResponse.role()
                ));
    }

    @GetMapping("/{id}/is-admin")
    public Mono<Boolean> isAdmin(@PathVariable Long id) {
        return userRepository.existsByIdAndRole(id, UserRole.ADMIN);
    }

    @GetMapping("/{id}/role")
    public Mono<UserRole> getUserRole(@PathVariable Long id) {
        return userRepository.findRoleById(id);
    }

    @GetMapping("/{currentUserId}/can-manage/{targetUserId}")
    public Mono<Boolean> canManageUser(@PathVariable Long currentUserId, @PathVariable Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            return Mono.just(true);
        }
        return userRepository.existsByIdAndRole(currentUserId, UserRole.ADMIN);
    }
}