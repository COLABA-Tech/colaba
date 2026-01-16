package com.example.colaba.user.controller;

import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.service.UserServicePublic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users Public", description = "API for managing users")
public class UserController extends BaseController {
    private final UserServicePublic userService;

    @GetMapping("/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves a user by their unique username.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Mono<ResponseEntity<UserResponse>> getUserByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal Long currentUserId) {
        return userService.getUserByUsername(username, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination", description = "Retrieves a paginated list of all users. Supports standard Spring Pageable parameters (page, size, sort).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of users")
    })
    public Mono<ResponseEntity<Page<UserResponse>>> getAllUsers(
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        return userService.getAllUsers(pageable, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/scroll")
    @Operation(summary = "Get users with infinite scroll", description = "Retrieves users for infinite scrolling using cursor-based pagination. Limit capped at 50.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scroll response with users, nextCursor, and hasMore flag")
    })
    public Mono<ResponseEntity<UserScrollResponse>> getUsersScroll(
            @RequestParam(defaultValue = "0") String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal Long currentUserId) {
        if (limit > 50) limit = 50;
        return userService.getUsersScroll(cursor, limit, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all users with pagination and total count header", description = "Retrieves a paginated list of all users with X-Total-Count header. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of users with total count header")
    })
    public Mono<ResponseEntity<Page<UserResponse>>> getUsersPaginated(
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        return userService.getAllUsers(pageable, currentUserId)
                .map(users -> ResponseEntity.ok()
                        .header("X-Total-Count", String.valueOf(users.getTotalElements()))
                        .body(users));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Partially updates a user by ID (username or email). Validates for duplicates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate username/email"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Mono<ResponseEntity<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return userService.updateUser(id, request, currentUserId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public Mono<ResponseEntity<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return userService.deleteUser(id, currentUserId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
