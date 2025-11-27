package com.example.colaba.user.controller;

import com.example.colaba.shared.controller.BaseController;
import com.example.colaba.shared.dto.user.CreateUserRequest;
import com.example.colaba.shared.dto.user.UpdateUserRequest;
import com.example.colaba.shared.dto.user.UserResponse;
import com.example.colaba.shared.dto.user.UserScrollResponse;
import com.example.colaba.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for managing users")
public class UserController extends BaseController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided username and email. Validates for duplicates.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate username/email")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves a user by their unique username.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination", description = "Retrieves a paginated list of all users. Supports standard Spring Pageable parameters (page, size, sort).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of users")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/scroll")
    @Operation(summary = "Get users with infinite scroll", description = "Retrieves users for infinite scrolling using cursor-based pagination. Limit capped at 50.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scroll response with users, nextCursor, and hasMore flag")
    })
    public ResponseEntity<UserScrollResponse> getUsersScroll(@RequestParam(defaultValue = "0") String cursor,
                                                             @RequestParam(defaultValue = "20") int limit) {
        if (limit > 50) limit = 50;
        UserScrollResponse response = userService.getUsersScroll(cursor, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all users with pagination and total count header", description = "Retrieves a paginated list of all users with X-Total-Count header. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of users with total count header")
    })
    public ResponseEntity<Page<UserResponse>> getUsersPaginated(Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.getTotalElements()))
                .body(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Partially updates a user by ID (username or email). Validates for duplicates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate username/email"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
